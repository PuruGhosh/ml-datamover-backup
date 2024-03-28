package com.xperi.datamover.controller;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

//if you need to test this on ur box, locally comment the @DisabledOnOs annotation. Depending on ur h/w it might work.
//also a bug (sometimes): for reference https://stackoverflow.com/questions/59877538/spring-kafka-embedded-topic-already-exists-between-tests
@RunWith(SpringRunner.class)
@DisabledOnOs({OS.WINDOWS, OS.LINUX,OS.MAC})
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    topics = {"${datamover.minioSubJobTopic}", "${asset.minioSubJobTopic}"},
    brokerProperties = {"listeners=PLAINTEXT://${spring.kafka.bootstrap-servers}", "port=9092"})
class EmbeddedDBDataMoverControllerIT extends BaseDataMoverControllerIT {

  private MongodExecutable mongodExecutable;
  private @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;

  /**
   * start mongo
   *
   * @throws Exception
   */
  @BeforeAll
  void startMongoDBEmbedded() throws Exception {

    final int port = 27017;
    final IMongodConfig mongodConfig =
        new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(port, Network.localhostIsIPv6()))
            .build();
    final MongodStarter starter = MongodStarter.getDefaultInstance();
    mongodExecutable = starter.prepare(mongodConfig);
    mongodExecutable.start();
  }

  /** stop the mongoDB */
  @AfterAll
  void cleanDB() {
    mongodExecutable.stop();
    embeddedKafkaBroker.getKafkaServer(0).shutdown();
  }
}
