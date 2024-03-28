package com.xperi.datamover.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * his class is to handle the error occurred while deserializing the records received from the
 * topic.
 */
@Slf4j
@Component
public class KafkaConsumerErrorHandler
    implements KafkaListenerErrorHandler, ContainerAwareErrorHandler {
  /**
   * This method handles the error for Kafka listener. We are logging exception for now, We will
   * handle in better way in later phase.
   */
  @Override
  public void handle(
      Exception thrownException,
      List<ConsumerRecord<?, ?>> records,
      Consumer<?, ?> consumer,
      MessageListenerContainer container) {
    log.error(
        "Error while deserializing kafka Topic {}", thrownException, thrownException.getMessage());
  }

  /**
   * This method handles the error for Kafka listener. We are logging exception for now, We will
   * handle in better way in later phase.
   *
   * @param message - the spring-messaging message.
   * @param exception - the exception the listener threw, wrapped in a
   *     ListenerExecutionFailedException.
   * @return the return value is ignored unless the annotated method has a @SendTo annotation.
   */
  @Override
  public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
    log.error("Kafka Listener Error {}", exception, exception.getMessage());
    return null;
  }
}
