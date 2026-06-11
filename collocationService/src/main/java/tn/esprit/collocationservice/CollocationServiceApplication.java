package tn.esprit.collocationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CollocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollocationServiceApplication.class, args);
    }

}
