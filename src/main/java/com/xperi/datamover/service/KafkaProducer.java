package com.xperi.datamover.service;

import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/** This class contains the method for producing message for asset sub job. */
@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducer {


  private final KafkaTemplate<String, AssetSubJobEvent> kafkaTemplate;
  private final KafkaTemplate<String, AssetMetadata> kafkaIndexTemplate;

  public void sendMessage(String topicName, AssetSubJobEvent assetSubJobEvent) {

    ListenableFuture<SendResult<String, AssetSubJobEvent>> future =
        kafkaTemplate.send(topicName, assetSubJobEvent);

    future.addCallback(
        new ListenableFutureCallback<SendResult<String, AssetSubJobEvent>>() {

          @Override
          public void onSuccess(SendResult<String, AssetSubJobEvent> result) {
            log.debug(
                "Sent message = {} with offset = {}",
                result.getProducerRecord().value(),
                result.getRecordMetadata().offset());
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Unable to send message due to : {}", ex, ex.getMessage());
          }
        });
  }

  public void sendMessage(String topicName, AssetMetadata assetMetadata){
    ListenableFuture<SendResult<String, AssetMetadata>> future =
        kafkaIndexTemplate.send(topicName, assetMetadata);

    future.addCallback(
        new ListenableFutureCallback<SendResult<String, AssetMetadata>>() {

            @Override
            public void onFailure(Throwable ex) {
                      log.error("Unable to send message", ex);
            }

            @Override
            public void onSuccess(SendResult<String, AssetMetadata> result) {
                      log.debug(
                              "Sent message = {} with offset = {}",
                              result.getProducerRecord().value(),
                              result.getRecordMetadata().offset());
            }
            }
        );
  }


}
