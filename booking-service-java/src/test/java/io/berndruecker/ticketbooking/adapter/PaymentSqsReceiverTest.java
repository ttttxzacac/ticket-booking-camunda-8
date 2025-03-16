package io.berndruecker.ticketbooking.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentSqsReceiverTest {

    @Mock
    private ZeebeClient zeebeClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SqsClient sqsClient;

    @InjectMocks
    private PaymentSqsReceiver paymentSqsReceiver;

    @Mock
    private PublishMessageCommandStep1 publishMessageCommandStep1;

    @Mock
    private PublishMessageCommandStep1.PublishMessageCommandStep2 publishMessageCommandStep2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject paymentResponseQueueUrl

    }

    // Check if json throws errors, some methods won't be invoked
    @Test
    void testPollSqsMessages_jsonParseException() throws Exception {
        // Mock SQS message
        Message sqsMessage = Message.builder()
                .body("invalid-json")
                .receiptHandle("dummy-receipt")
                .build();
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(List.of(sqsMessage))
                .build();
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);

        // Mock JSON parsing throws exception
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("JSON parse error"));

        // Call method (should catch exception internally)
        paymentSqsReceiver.pollSqsMessages();

        // Verify Zeebe not called
        verify(zeebeClient, never()).newPublishMessageCommand();

        // Verify deleteMessage not called
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
