package com.local.SummarizerOrchestrator.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for customizing thread pool settings used for asynchronous task execution.
 *
 * <p>This class defines a {@link ThreadPoolTaskExecutor} bean named {@code asyncExecutor}.
 * It is utilized to handle tasks annotated with {@link org.springframework.scheduling.annotation.Async},
 * ensuring efficient and controlled asynchronous execution.</p>
 *
 * <p>Key configurations:
 * <ul>
 *     <li><strong>Core Pool Size:</strong> Minimum number of threads kept alive.</li>
 *     <li><strong>Max Pool Size:</strong> Maximum number of threads in the pool.</li>
 *     <li><strong>Queue Capacity:</strong> Maximum tasks that can be queued when all threads are busy.</li>
 *     <li><strong>Thread Name Prefix:</strong> A prefix for thread names, aiding in debugging.</li>
 * </ul>
 * </p>
 */
@Configuration
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 6;  // Minimum number of threads always kept alive
    private static final int MAX_POOL_SIZE = 12;  // Maximum number of threads allowed in the pool
    private static final int QUEUE_CAPACITY = 32;  // Queue size for tasks waiting to be executed
    private static final String THREAD_NAME_PREFIX = "SummarizationThread-";  // Prefix for thread names

    /**
     * Configures and provides a {@link ThreadPoolTaskExecutor} bean for asynchronous task execution.
     *
     * <p>Threads in the executor pool are managed dynamically within the specified bounds, ensuring
     * optimal resource utilization while processing asynchronous tasks.</p>
     *
     * @return A fully configured {@link Executor} instance for asynchronous execution.
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}
