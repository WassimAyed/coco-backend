package tn.esprit.subspaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class SubsPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubsPaymentServiceApplication.class, args);
    }
}
