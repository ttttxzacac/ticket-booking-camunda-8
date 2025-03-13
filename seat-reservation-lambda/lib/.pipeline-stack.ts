import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as apigw from "aws-cdk-lib/aws-apigateway";
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as path from 'path';

export class SeatReservationLambdaStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, 'ImportedVPC', {
      isDefault: true,
      vpcId: 'vpc-0316cf747ad31953a',
    });

    const vpcEndpoint = new ec2.InterfaceVpcEndpoint(this, 'ApiGatewayVpcEndpoint', {
      vpc,
      service: ec2.InterfaceVpcEndpointAwsService.APIGATEWAY,
      privateDnsEnabled: true, // Ensures API Gateway resolves to the private IP inside AWS
    });

    const fn = new lambda.Function(this, 'SeatReservationLambda', {
      functionName: 'seat-reservation-lambda',
      runtime: lambda.Runtime.NODEJS_18_X,
      handler: 'index.handler',
      code: lambda.Code.fromAsset(path.join(__dirname, 'handler')),
      memorySize: 128,
    });

    const api = new apigw.RestApi(this, 'SeatReservationApi', {
      restApiName: 'SeatReservationApi',
      deployOptions: {
        stageName: 'v1',
      },
      endpointConfiguration: {
        types: [apigw.EndpointType.PRIVATE],
        vpcEndpoints: [vpcEndpoint],
      },
      policy: new iam.PolicyDocument({
        statements: [
          new iam.PolicyStatement({
            effect: iam.Effect.DENY,
            principals: [new iam.AnyPrincipal()],
            actions: ['execute-api:Invoke'],
            resources: ['*'],
            conditions: {
              'StringNotEquals': {
                'aws:SourceVpc': vpc.vpcId,
              },
            },
          }),
        ],
      }),
    });

    const reservationResource = api.root.addResource('seat-reservation');
    reservationResource.addMethod('POST', new apigw.LambdaIntegration(fn));

    new cdk.CfnOutput(this, 'ApiGatewayUrlOutput', {
      value: api.url,
      description: 'The endpoint for the API Gateway triggering the Lambda function (only accessible within VPC)',
      exportName: 'SeatReservationRestApiUrl'
    });
  }
}
