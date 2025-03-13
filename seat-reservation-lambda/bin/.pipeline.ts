#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { PipelineStack } from '../lib/.pipeline-stack';

const app = new cdk.App();
new PipelineStack(app, 'PipelineStack', { env: { account: '515966493420', region: 'eu-north-1' } });
