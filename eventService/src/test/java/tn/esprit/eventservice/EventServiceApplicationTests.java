package tn.esprit.eventservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootTest
@EnableConfigurationProperties
@EnableScheduling
class EventServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
