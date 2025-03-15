package io.berndruecker.ticketbooking.rest;

import io.berndruecker.ticketbooking.adapter.GenerateTicketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootConfiguration
@RestController
@EnableZeebeClient
public class StatusRestController {

    @Autowired
    private ZeebeClient client;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient webClient ;


    @GetMapping("/status")
    public String getStatus() {
        Topology topology = client.newTopologyRequest().send().join();
        return topology.toString();
    }

    @GetMapping("/status2")
    public String getStatus2() {
         String ENDPOINT = "https://31htvbz10i.execute-api.eu-central-1.amazonaws.com/default/ticket-generate-tianshen";
        GenerateTicketAdapter.CreateTicketResponse ticket = restTemplate.getForObject(ENDPOINT, GenerateTicketAdapter.CreateTicketResponse.class);
        return ticket.ticketId;
    }

    @GetMapping("/status3")
    public String getStatus3() {
        String ENDPOINT = "https://p7biv6lqa2.execute-api.eu-central-1.amazonaws.com/default/ticket-generate-tianshen";
        GenerateTicketAdapter.CreateTicketResponse ticket = restTemplate.getForObject(ENDPOINT, GenerateTicketAdapter.CreateTicketResponse.class);
        return ticket.ticketId;
    }

    @GetMapping("/status4")
    public String getStatus4() {
        String ENDPOINT = "https://p7biv6lqa2.execute-api.eu-central-1.amazonaws.com/default/ticket-generate-tianshen";

        GenerateTicketAdapter.CreateTicketResponse ticket = webClient.get()
                .uri(ENDPOINT)
                .retrieve()
                .bodyToMono(GenerateTicketAdapter.CreateTicketResponse.class)
                .block(); //

        return ticket != null ? ticket.ticketId : "No Ticket ID Received";
    }

    @GetMapping("/status5")
    public String getStatus5() {
        String ENDPOINT = "https://spwwlr7zk9.execute-api.eu-central-1.amazonaws.com/stage1/";
        GenerateTicketAdapter.CreateTicketResponse ticket = restTemplate.getForObject(ENDPOINT, GenerateTicketAdapter.CreateTicketResponse.class);
        return ticket.ticketId;
    }


    
}
