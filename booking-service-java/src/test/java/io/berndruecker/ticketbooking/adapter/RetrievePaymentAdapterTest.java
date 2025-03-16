package io.berndruecker.ticketbooking.adapter;

import io.berndruecker.ticketbooking.ProcessConstants;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetrievePaymentAdapterTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ActivatedJob activatedJob;

    @InjectMocks
    private RetrievePaymentAdapter retrievePaymentAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject paymentRequestQueueUrl manually
        ReflectionTestUtils.setField(retrievePaymentAdapter, "paymentRequestQueueUrl", "dummy-queue-url");
    }

    /* Check method will return a valid UUID
     Check SQS sends message that contains correct payment request ID
     */
    @Test
    void testRetrievePayment_successfulSend() {
        // Mock SQS sendMessage response
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId(UUID.randomUUID().toString()).build()); // mock SQS,  no matter what send to SQS, return a valid messageId

        // Call method
        Map<String, Object> result = retrievePaymentAdapter.retrievePayment(activatedJob);

        // Assertions: Check if paymentRequestId exists and is UUID format
        assertTrue(result.containsKey(ProcessConstants.VAR_PAYMENT_REQUEST_ID));
        String paymentRequestId = (String) result.get(ProcessConstants.VAR_PAYMENT_REQUEST_ID); // retrievePayment() result
        assertDoesNotThrow(() -> UUID.fromString(paymentRequestId)); // Valid UUID check

        // Verify SQS sendMessage was called
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class); // get arugument  passed to SQS
        verify(sqsClient, times(1)).sendMessage(captor.capture()); // make sure SQS sendMessage was called once

        // Verify message content
        SendMessageRequest sentRequest = captor.getValue();
        assertEquals("dummy-queue-url", sentRequest.queueUrl());
        assertTrue(sentRequest.messageBody().contains(paymentRequestId));
    }
}
