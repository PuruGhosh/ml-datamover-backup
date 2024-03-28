package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** This class contains read properties with prefix "asset" */
@Configuration
@ConfigurationProperties(prefix = "asset")
@Data
public class AssetConfigProperties {

  /**
   * Topic name from which Kafka consumer will receive upload error messages & bucket event
   * notification
   */
  private String minioSubJobTopic;

  /** Endpoint for Minio server */
  private String minioEndPoint;

  /** Endpoint for getting the content of a file stored in minio */
  private String contentUrl;

  /** Kafka Topic name to receive asset indexing events. */
  private String indexMetadataTopic;
}
