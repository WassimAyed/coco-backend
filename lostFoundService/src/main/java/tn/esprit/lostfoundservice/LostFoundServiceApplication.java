package tn.esprit.lostfoundservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LostFoundServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LostFoundServiceApplication.class, args);
    }
}
