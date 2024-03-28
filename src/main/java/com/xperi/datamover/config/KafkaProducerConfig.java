package com.xperi.datamover.config;

import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/** This class contains the configuration for kafka producer
 * There are two types of producer configs for two Kafka templates present here.
 * One of them is configureKafkaTemplate(), which is used to generate a message using AssetSubJobEventSerializer.
 * Another one, configureKafkaTemplateForMetadata() is used to generate a message using the Avro Serializer.
 * */
@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfig {
  private final KafkaProperties kafkaProperties;
  private final AvroConfigProperties avroConfigProperties;

  @Bean
  public ProducerFactory<String, AssetSubJobEvent> configureProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, avroConfigProperties.getSchemaRegistryUrl());
    config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 90000);
    return new DefaultKafkaProducerFactory<>(config);
  }


  @Bean
  public KafkaTemplate<String, AssetSubJobEvent> configureKafkaTemplate() {
    return new KafkaTemplate<>(configureProducerFactory());
  }

  @Bean
  public ProducerFactory<String, AssetMetadata> configureProducerFactoryForMetadata() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, avroConfigProperties.getSchemaRegistryUrl());
    config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 90000);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, AssetMetadata> configureKafkaTemplateForMetadata() {
    return new KafkaTemplate<>(configureProducerFactoryForMetadata());
  }
}
