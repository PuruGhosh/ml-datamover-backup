package com.xperi.datamover.config;

import lombok.Data;

@Data
public class ThreadPoolConfig {
  private int corePoolSize = 4;
  private int maximumPoolSize = 10;

  private long keepAliveTime = 1;

  private String threadNamePrefix = "asset-";

  private int retryCount = 0;

}
