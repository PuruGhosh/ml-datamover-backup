package com.xperi.datamover.service;

import com.xperi.datamover.config.AccumulatorConfigProperties;
import com.xperi.datamover.config.AssetConfigProperties;
import com.xperi.datamover.model.minio.BucketEvent;
import com.xperi.schema.accumulator.AccumulatorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/** This class contains the method for consuming event notifications from MinIo. */
@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaConsumer {

  private final AssetConfigProperties assetConfigProperties;
  private final AccumulatorConfigProperties accumulatorConfigProperties;
  private final KafkaConsumerErrorHandler kafkaConsumerErrorHandler;
  private final AssetSubJobService assetSubJobService;

  @KafkaListener(
      topics = "#{assetConfigProperties.minioSubJobTopic}",
      containerFactory = "configureKafkaListenerContainerFactory",
      errorHandler = "kafkaConsumerErrorHandler")
  public void consume(ConsumerRecord<String, BucketEvent> record, Acknowledgment acknowledgment) {
    log.debug("Received event notification from Minio: {}", record.value());
    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(record.value());
    acknowledgment.acknowledge();
  }

  /**
   * To consume the messages from Kafka topic: update.accumulator.topic
   * @param record
   * @param acknowledgment
   */
  @KafkaListener(
          topics = "#{accumulatorConfigProperties.topicName}",
          containerFactory = "configureAccumulatorKafkaListenerContainerFactory",
          errorHandler = "kafkaConsumerErrorHandler")
  public void consumeAccumulator(ConsumerRecord<String, AccumulatorEvent> record, Acknowledgment acknowledgment) {
    log.debug("Received event notification from Minio: {}", record.value());
    assetSubJobService.updateSubJobStatusForIndexSuccessAndAccumulatorIncrement(record.value());
    acknowledgment.acknowledge();
  }
}
