package io.berndruecker.ticketbooking;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.regions.Region;
@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = { "classpath:ticket-booking.bpmn" })
@EnableScheduling
public class TicketBookingApplication {


  public static void main(String[] args) {
    System.out.println("running 1");
    SpringApplication.run(TicketBookingApplication.class, args);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public Queue paymentResponseQueue(){
    return new Queue("paymentResponse",true);
  }

  @Bean
  public SqsClient sqsClient(@Value("${aws.region:eu-central-1}") String awsRegion) {
    return SqsClient.builder()
            .region(Region.of(awsRegion))
            .build();
  }

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
            .baseUrl("https://api.example.com") // 可以配置Base URL
            .defaultHeader("Authorization", "Bearer YOUR_ACCESS_TOKEN") // 默认Header
            .build();
  }




}
