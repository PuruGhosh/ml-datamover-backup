package com.xperi.datamover.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

// Disabled as target environment requires Docker installation.
// CI environment runs the test in a dedicated docker container, which
// can't spawn other container from within.
@Testcontainers
@DisabledOnOs({OS.WINDOWS, OS.LINUX,OS.MAC})
@EmbeddedKafka(
  partitions = 1,
  topics = {"${datamover.minioSubJobTopic}", "${asset.minioSubJobTopic}"},
  brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=9092"})
public class TestContainerDataMoverControllerIT extends BaseDataMoverControllerIT {

  @Container static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
  static {
    mongoDBContainer.start();
  }

  private @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;

  /**
   * overriding the property for the host and the port
   *
   * @param registry
   */
  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @AfterAll
  void cleanDB() {
    embeddedKafkaBroker.getKafkaServer(0).shutdown();
  }
}
