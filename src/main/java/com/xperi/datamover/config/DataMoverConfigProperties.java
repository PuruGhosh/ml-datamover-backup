package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** This class contains read properties with prefix "datamover" */
@Configuration
@ConfigurationProperties(prefix = "datamover")
@Data
public class DataMoverConfigProperties {

  /** Base storage path for assets */
  private String assetStoragePath;

  /** Topic name to which Asset sub job messages will be sent */
  private String minioSubJobTopic;

  /** No of Kafka consumer instance */
  private String noOfConsumerInstance;

  /** Minimum parallel threads that will run at same time */
  private int corePoolSize;

  /** Maximum parallel threads that will run at same time */
  private int maxPoolSize;

  /** Capacity when all pool size is filled */
  private int queueCapacity;

  /** Number of items in each page for pagination */
  private int sizeOfPage;
}
