#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { SeatReservationLambdaStack } from '../lib/.pipeline-stack';

const region = process.env.CDK_DEFAULT_REGION;

const app = new cdk.App();
new SeatReservationLambdaStack(app, 'SeatReservationLambdaStack', { env: { region } });
