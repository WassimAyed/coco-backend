package tn.esprit.serviceetudiant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dedicated thread pool for ML moderation calls.
     * Keeps moderation work isolated so slow Ollama responses
     * never affect other application threads.
     */
    @Bean(name = "moderationExecutor")
    public Executor moderationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ml-moderation-");
        executor.initialize();
        return executor;
    }
}
