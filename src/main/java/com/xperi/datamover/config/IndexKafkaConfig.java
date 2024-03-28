package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** This class contains read properties with prefix "asset" */
@Configuration
@ConfigurationProperties(prefix = "index")
@Data
public class IndexKafkaConfig {

  /** Endpoint for bootstrap server of the kafka topic */
  private String bootstrapServers;

  /** Group id of the kafka topic */
  private String groupId;

  /** Auto offset reset status of the kafka topic */
  private String autoOffsetReset;

}
