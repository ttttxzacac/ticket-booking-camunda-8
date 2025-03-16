package io.berndruecker.ticketbooking.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

// //batch delete
// import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
// import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
// import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchResult;

import io.camunda.zeebe.client.ZeebeClient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class PaymentSqsReceiver {

  private final Logger logger = LoggerFactory.getLogger(PaymentSqsReceiver.class);

  private final ZeebeClient client;
  private final ObjectMapper objectMapper;
  private final SqsClient sqsClient;

  @Value("${aws.sqs.paymentResponseQueueUrl}")
  private String paymentResponseQueueUrl;

  public PaymentSqsReceiver(ZeebeClient client, ObjectMapper objectMapper, SqsClient sqsClient) {
    this.client = client;
    this.objectMapper = objectMapper;
    this.sqsClient = sqsClient;
  }

  // Periodically poll SQS (executed every 1 seconds)
  @Scheduled(fixedRate = 5000) //5s
  @Transactional
  public void pollSqsMessages() {
    // 1. Get messages from SQS
    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(paymentResponseQueueUrl)
            .maxNumberOfMessages(4)
            .waitTimeSeconds(45)
            .build();
    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

    // // List to store batch delete requests
    // List<DeleteMessageBatchRequestEntry> deleteRequests = new ArrayList<>();
    
    for (Message message : messages) {
      try {
        logger.info("Received message: " + message);

        // 2. Parsing the message
        JsonNode rootNode = objectMapper.readTree(message.body());
        String innerJson = rootNode.get("message").asText();
        PaymentResponseMessage paymentResponse = objectMapper.readValue(innerJson, PaymentResponseMessage.class);
        // PaymentResponseMessage paymentResponse = objectMapper.readValue(message.body(), PaymentResponseMessage.class);
        logger.info("Received: " + paymentResponse);

        // 3. Send a message to Zeebe Workflow
        // client.newPublishMessageCommand()
        //         .messageName("msg-payment-received")
        //         .correlationKey(paymentResponse.paymentRequestId)
        //         .variables(Collections.singletonMap("paymentConfirmationId", paymentResponse.paymentConfirmationId))
        //         .send()
        //         .join();
        
        // 可以改成异步的
        client.newPublishMessageCommand()
                .messageName("msg-payment-received")
                .correlationKey(paymentResponse.paymentRequestId)
                .variables(Collections.singletonMap("paymentConfirmationId", paymentResponse.paymentConfirmationId))
                .send()
                .thenRun(() -> logger.info("Message sent successfully"));

        // 4. Delete the SQS message after successful processing
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(paymentResponseQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build());

        // // 4. Add the message to the batch delete list
        // deleteRequests.add(DeleteMessageBatchRequestEntry.builder()
        //         .id(message.messageId())  // Unique identifier for the message
        //         .receiptHandle(message.receiptHandle())
        //         .build());

      } catch (Exception e) {
        logger.error("Error processing SQS message", e);
      }
    }
    // // Perform batch delete if there are any messages to delete
    // if (!deleteRequests.isEmpty()) {
    //   try {
    //     DeleteMessageBatchRequest batchRequest = DeleteMessageBatchRequest.builder()
    //             .queueUrl(paymentResponseQueueUrl)
    //             .entries(deleteRequests)
    //             .build();
    //     DeleteMessageBatchResult result = sqsClient.deleteMessageBatch(batchRequest);
    //     if (!result.failed().isEmpty()) {
    //       logger.error("Failed to delete some messages: " + result.failed());
    //     } else {
    //       logger.info("Successfully deleted messages from SQS.");
    //     }
    //   } catch (Exception e) {
    //     logger.error("Error performing batch delete", e);
    //   }
    // }
    
  }

  public static class PaymentResponseMessage {
    @JsonProperty("paymentRequestId")
    public String paymentRequestId;
    @JsonProperty("paymentConfirmationId")
    public String paymentConfirmationId;

    @Override
    public String toString() {
      return "PaymentResponseMessage [paymentRequestId=" + paymentRequestId + ", paymentConfirmationId=" + paymentConfirmationId + "]";
    }
  }
}
