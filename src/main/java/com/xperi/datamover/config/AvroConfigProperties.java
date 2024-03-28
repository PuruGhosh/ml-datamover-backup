package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** This class contains read properties with prefix "avro" */
@Configuration
@ConfigurationProperties(prefix = "avro")
@Data
public class AvroConfigProperties {
    /** Endpoint for Schema Registry URL */
    private String schemaRegistryUrl;
}
