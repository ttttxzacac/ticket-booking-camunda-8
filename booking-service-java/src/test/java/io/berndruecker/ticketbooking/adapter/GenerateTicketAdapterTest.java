package io.berndruecker.ticketbooking.adapter;



import com.fasterxml.jackson.databind.ObjectMapper;
import io.berndruecker.ticketbooking.ProcessConstants;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerateTicketAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ActivatedJob activatedJob;

    @InjectMocks
    private GenerateTicketAdapter generateTicketAdapter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Happy path check
    @Test
    void testCallGenerateTicketRestService_successfulResponse() throws IOException {
        // Mock ActivatedJob variables
        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_SIMULATE_BOOKING_FAILURE, "no-failure");
        when(activatedJob.getVariablesAsMap()).thenReturn(variables);

        // Mock REST API response
        String apiResponseJson = "{ \"statusCode\": 200, \"headers\": {}, \"body\": \"{\\\"ticketId\\\":\\\"12345\\\"}\" }";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(apiResponseJson);

        // Call method
        Map<String, Object> result = generateTicketAdapter.callGenerateTicketRestService(activatedJob);

        // Verify result
        assertEquals("12345", result.get(ProcessConstants.VAR_TICKET_ID));

        // Verify restTemplate called once
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    // Simulate failure check
    @Test
    void testCallGenerateTicketRestService_simulateFailure() {
        // Mock ActivatedJob variables to simulate failure
        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_SIMULATE_BOOKING_FAILURE, "ticket");
        when(activatedJob.getVariablesAsMap()).thenReturn(variables);

        // Expect IOException
        assertThrows(IOException.class, () -> {
            generateTicketAdapter.callGenerateTicketRestService(activatedJob);
        });

        // Verify REST API not called
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }
}
