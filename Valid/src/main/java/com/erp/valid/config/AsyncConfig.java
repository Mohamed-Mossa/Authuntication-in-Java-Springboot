package com.erp.valid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // Always keep 2 threads ready
        executor.setMaxPoolSize(5);       // Maximum 5 threads if busy
        executor.setQueueCapacity(100);   // Can queue up to 100 tasks
        executor.setThreadNamePrefix("email-async-");
        executor.initialize();
        return executor;
    }
}