package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** This class contains read properties with prefix "accumulator" */
@Configuration
@ConfigurationProperties(prefix = "accumulator")
@Data
public class AccumulatorConfigProperties {
  /** Topic name to which ml-graph will produce messages after creating asset version */
  private String topicName;
  /** Value serializer for Accumulator Event message */
  private String valueDeserializer;
  /** No of Kafka consumer instance */
  private String noOfConsumerInstance;
}