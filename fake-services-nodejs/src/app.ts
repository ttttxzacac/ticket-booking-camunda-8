const { v4: uuidv4 } = require('uuid');

////////////////////////////////////
// FAKE SEAT RESERVATION SERVICE
////////////////////////////////////
import { ZBClient } from "zeebe-node";
require("dotenv").config();

const zeebeClient = new ZBClient();
const worker = zeebeClient.createWorker('reserve-seats', reserveSeatsHandler)

function reserveSeatsHandler(job, _, worker) {  
  console.log("\n\n Reserve seats now...");
  console.log(job);

  // Do the real reservation
  // TODO: Fake some results! Fake an error (when exactly?)
  if ("seats" !== job.variables.simulateBookingFailure) {
    console.log("Successul :-)");
    return job.complete({
        reservationId: "1234",
      });
  } else {
    console.log("ERROR: Seats could not be reserved!");
    return job.error("ErrorSeatsNotAvailable");
  }
}




////////////////////////////////////
// FAKE PAYMENT SERVICE
////////////////////////////////////
var amqp = require('amqplib/callback_api');

const queuePaymentRequest = 'paymentRequest';
const queuePaymentResponse = 'paymentResponse';

const RABBITMQ_HOST = process.env.RABBITMQ_HOST;
const RABBITMQ_USER = process.env.RABBITMQ_USER;
const RABBITMQ_PASSWORD = process.env.RABBITMQ_PASSWORD;
// I have modified the code, it can read parameters from .env
amqp.connect(`amqp://${RABBITMQ_USER}:${RABBITMQ_PASSWORD}@${RABBITMQ_HOST}`, function(error0, connection) {
  if (error0) {
    throw error0;
  }
  connection.createChannel(function(error1, channel) {
    if (error1) {
      throw error1;
    }
    
    channel.assertQueue(queuePaymentRequest, { durable: true });
    channel.assertQueue(queuePaymentResponse, {durable: true });

    channel.consume(queuePaymentRequest, function(inputMessage) {
      var paymentRequestId =  inputMessage.content.toString();
      var paymentConfirmationId = uuidv4();

      console.log("\n\n [x] Received payment request %s", paymentRequestId);

      var outputMessage = '{"paymentRequestId": "' + paymentRequestId + '", "paymentConfirmationId": "' + paymentConfirmationId + '"}';

      channel.sendToQueue(queuePaymentResponse, Buffer.from(outputMessage));
      console.log(" [x] Sent payment response %s", outputMessage);
  
    }, {
        noAck: true
    });
  });
});



////////////////////////////////////
// FAKE TICKET GENERATION SERVICE
////////////////////////////////////
var express = require("express");
var app = express();

app.listen(3000, () => {
  console.log("HTTP Server running on port 3000");
});

app.get("/ticket", (req, res, next) => {
  var ticketId = uuidv4();
  console.log("\n\n [x] Create Ticket %s", ticketId);
  res.json({"ticketId" : ticketId});
});