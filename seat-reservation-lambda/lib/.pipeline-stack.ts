import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as apigw from "aws-cdk-lib/aws-apigateway";
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as path from 'path';

export class SeatReservationLambdaStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const fn = new lambda.Function(this, 'SeatReservationLambda', {
      functionName: 'seat-reservation-lambda',
      runtime: lambda.Runtime.NODEJS_18_X,
      handler: 'index.handler',
      code: lambda.Code.fromAsset(path.join(__dirname, 'handler')),
      memorySize: 128,
    });

    const api = new apigw.LambdaRestApi(this, `ApiGwEndpoint`, {
      handler: fn,
      restApiName: 'SeatReservationApi',
    });

    new cdk.CfnOutput(this, 'ApiGatewayUrlOutput', {
      value: api.url,
      description: 'The endpoint for the API Gateway triggering the Lambda function',
      exportName: 'SeatReservationLambdaRestApiUrl'
    });
  }
}
