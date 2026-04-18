package com.app.quantitymeasurement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous method execution.
 * Provides a thread pool for async operations like email sending.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates a thread pool executor for async operations.
     * Used by @Async methods like EmailService.sendPasswordResetEmail().
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-executor-");
        executor.initialize();
        return executor;
    }
}