/** */
package com.xperi.datamover.config;

import com.xperi.datamover.task.JobExecutorService;
import com.xperi.datamover.task.TaskQueue;
import com.xperi.datamover.task.TaskRejectedExecutionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.BlockingQueue;

/** */
@Configuration
public class AppConfig {

  @Autowired private ThreadPoolConfig jobThreadPoolConfig;

  @Autowired private ThreadPoolConfig subjobThreadPoolConfig;

  @Bean
  @ConfigurationProperties(prefix = "job.thread-pool")
  public ThreadPoolConfig jobThreadPoolConfig() {
    return new ThreadPoolConfig();
  }

  @Bean
  @ConfigurationProperties(prefix = "subjob.thread-pool")
  public ThreadPoolConfig subjobThreadPoolConfig() {
    return new ThreadPoolConfig();
  }

  @Bean
  public JobExecutorService jobExecutorService() {
    final BlockingQueue<Runnable> taskQueue = new TaskQueue<>();
    final TaskRejectedExecutionHandler handler = new TaskRejectedExecutionHandler();
    final CustomizableThreadFactory factory =
        new CustomizableThreadFactory(jobThreadPoolConfig.getThreadNamePrefix());
    final JobExecutorService executer =
        new JobExecutorService(jobThreadPoolConfig, taskQueue, factory, handler);

    return executer;
  }

  @Bean
  public JobExecutorService subJobExecutorService() {
    final BlockingQueue<Runnable> taskQueue = new TaskQueue<>();
    final TaskRejectedExecutionHandler handler = new TaskRejectedExecutionHandler();
    final CustomizableThreadFactory factory =
        new CustomizableThreadFactory(subjobThreadPoolConfig.getThreadNamePrefix());
    final JobExecutorService executer =
        new JobExecutorService(subjobThreadPoolConfig, taskQueue, factory, handler);

    return executer;
  }
}
