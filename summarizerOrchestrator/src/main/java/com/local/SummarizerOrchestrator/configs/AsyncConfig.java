package com.local.SummarizerOrchestrator.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for defining custom thread pool settings used for asynchronous
 * execution in the application.
 *
 * <p>This configuration defines a {@link ThreadPoolTaskExecutor} bean named {@code asyncExecutor},
 * which is used to execute tasks annotated with {@link org.springframework.scheduling.annotation.Async}.
 * The executor supports a limited number of concurrent threads and queues additional tasks when necessary.</p>
 */
@Configuration
public class AsyncConfig {

    /**
     * Creates and configures a {@link ThreadPoolTaskExecutor} bean for handling asynchronous tasks.
     *
     * <p>The executor is configured with the following settings:
     * <ul>
     *     <li><strong>Core Pool Size:</strong> Minimum number of threads always kept alive.</li>
     *     <li><strong>Max Pool Size:</strong> Maximum number of threads allowed in the pool.</li>
     *     <li><strong>Queue Capacity:</strong> Number of tasks that can be queued when all threads are busy.</li>
     *     <li><strong>Thread Name Prefix:</strong> Prefix for thread names, useful for debugging.</li>
     * </ul>
     * </p>
     *
     * @return A configured {@link Executor} instance for asynchronous task execution.
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(32);
        executor.setThreadNamePrefix("SummarizationThread-");
        executor.initialize();
        return executor;
    }
}
