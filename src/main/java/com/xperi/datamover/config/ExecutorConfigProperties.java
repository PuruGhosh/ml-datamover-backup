package com.xperi.datamover.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class ExecutorConfigProperties {

  private final DataMoverConfigProperties dataMoverConfigProperties;
  /**
   * Executor for the creation of new sub Jobs asynchronously
   *
   */
  @Bean("SubJobCreationExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(dataMoverConfigProperties.getCorePoolSize());
    executor.setMaxPoolSize(dataMoverConfigProperties.getMaxPoolSize());
    executor.setQueueCapacity(dataMoverConfigProperties.getQueueCapacity());
    executor.initialize();
    return executor;
  }
}
