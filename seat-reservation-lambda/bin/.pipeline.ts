#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { SeatReservationLambdaStack } from '../lib/.pipeline-stack';

const app = new cdk.App();
new SeatReservationLambdaStack(app, 'SeatReservationLambdaStack', { env: { account: '515966493420', region: 'eu-north-1' } });
