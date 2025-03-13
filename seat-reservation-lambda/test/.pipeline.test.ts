import * as cdk from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import * as Pipeline from '../lib/.pipeline-stack';

test('Lambda Created', () => {
  const app = new cdk.App();

  // WHEN
  const stack = new Pipeline.SeatReservationLambdaStack(app, 'MyTestStack');

  // THEN
  const template = Template.fromStack(stack);

  template.hasResourceProperties('AWS::Lambda::Function', {
    FunctionName: 'seat-reservation-lambda',
    Runtime: 'nodejs18.x',
    MemorySize: 128,
  });

  template.hasResource('AWS::ApiGateway::RestApi', {});

  template.hasOutput('ApiGatewayUrlOutput', {
    Export: { Name: 'SeatReservationRestApiUrl' },
  });
});
