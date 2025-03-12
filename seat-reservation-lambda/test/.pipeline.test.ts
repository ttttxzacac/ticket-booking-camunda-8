import * as cdk from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import * as Pipeline from '../lib/.pipeline-stack';

test('Lambda Created', () => {
   const app = new cdk.App();
   
   // WHEN
   const stack = new Pipeline.PipelineStack(app, 'MyTestStack');
   
   // THEN
   const template = Template.fromStack(stack);

   template.hasResourceProperties('AWS::Lambda::Function', {
     FunctionName: 'seat-reservation-lambda', 
     MemorySize: 128, 
   });
});
