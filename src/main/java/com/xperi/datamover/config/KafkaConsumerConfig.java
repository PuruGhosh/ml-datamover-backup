package com.xperi.datamover.config;

import com.xperi.datamover.model.minio.BucketEvent;
import com.xperi.schema.accumulator.AccumulatorEvent;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the configurations for Kafka Consumer. For consuming messages, we need to
 * configure a ConsumerFactory and a KafkaListenerContainerFactory. Once these beans are available
 * in the Spring bean factory, consumers can be configured using @KafkaListener
 * annotation. @EnableKafka annotation is required on the configuration class to enable detection
 * of @KafkaListener annotation on spring managed beans.
 */
@Getter
@RequiredArgsConstructor
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  private final KafkaProperties kafkaProperties;
  private final DataMoverConfigProperties dataMoverConfigProperties;
  private final AccumulatorConfigProperties accumulatorConfigProperties;

  private final AvroConfigProperties avroConfigProperties;

  @Bean
  public ConsumerFactory<String, BucketEvent> configureConsumerFactory() {
    return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory configureKafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, BucketEvent>();
    factory.setConsumerFactory(configureConsumerFactory());
    factory.setConcurrency(Integer.valueOf(dataMoverConfigProperties.getNoOfConsumerInstance()));
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, AccumulatorEvent> configureAccumulatorConsumerFactory() {
    // return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
    config.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        kafkaProperties.getConsumer().getAutoOffsetReset());
    config.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        kafkaProperties.getConsumer().getEnableAutoCommit());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    config.put(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        avroConfigProperties.getSchemaRegistryUrl());
    config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    ConsumerFactory<String, AccumulatorEvent> consumerFactory =
        new DefaultKafkaConsumerFactory<>(config);
    return consumerFactory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory
      configureAccumulatorKafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, AccumulatorEvent>();
    factory.setConsumerFactory(configureAccumulatorConsumerFactory());
    factory.setConcurrency(Integer.valueOf(accumulatorConfigProperties.getNoOfConsumerInstance()));
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }
}
