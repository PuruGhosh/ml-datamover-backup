package com.xperi.datamover.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.model.AssetEvent;
import com.xperi.datamover.model.minio.BucketEvent;
import com.xperi.datamover.util.KafkaUtil;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.subjobevent.AssetCategory;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbeddedKafka(
    partitions = 1,
    topics = {"testTopic"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class EmbeddedKafkaIT {
  private static final String TEST_TOPIC = "testTopic";
  private final KafkaUtil kafkaUtil = new KafkaUtil();
  private final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private EmbeddedKafkaBroker embeddedKafkaBroker;
  private KafkaTemplate<String, AssetSubJobEvent> kafkaTemplate;
  private KafkaTemplate<String, AssetMetadata> kafkaIndexTemplate;
  private KafkaProducer kafkaProducer;

  @BeforeEach
  void setUp(EmbeddedKafkaBroker embeddedKafkaBroker) {
    this.embeddedKafkaBroker = embeddedKafkaBroker;
    SchemaRegistryClient client = new MockSchemaRegistryClient();
    this.kafkaTemplate = new KafkaTemplate<>(configureKafkaTemplateAssetSubJob());
    this.kafkaProducer = new KafkaProducer(this.kafkaTemplate,this.kafkaIndexTemplate);
  }

  @Test
  @DisplayName("Integration Testing of Minio BucketEvent Json parser")
  public void testMinioAssetEventParser() throws Exception {
    URL resource = getClass().getClassLoader().getResource("minio_sample_event.json");
    byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
    String minioEventString = new String(bytes);

    try(var consumer = configureConsumerProp();
        var producer = configureProducer()) {
      producer.send(new ProducerRecord<>(TEST_TOPIC, "", minioEventString));
      ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, TEST_TOPIC);
      BucketEvent bucketEvent = objectMapper.readValue(record.value(), BucketEvent.class);
      AssetEvent assetEvent = kafkaUtil.parseMinioEvent(bucketEvent);

      assertEquals("assets", assetEvent.getBucketName(), "Bucket name is not equal");
      assertEquals("assets/test.png", assetEvent.getKey(), "Key name is not equal");
      assertEquals(368, assetEvent.getAssetSize(), "Asset size is not equal");
      assertEquals("image/png", assetEvent.getContentType(), "Asset's content type is not equal");
      assertEquals("1", assetEvent.getVersionId(), "Asset version id is not equal");
      assertEquals("admin", assetEvent.getOwnerId(), "Owner id is not equal");
    }
  }

  @Test
  @DisplayName("Testing Kafka Producer")
  public void testKafkaProducer() throws IOException {
    AssetSubJobEvent assetSubJobEvent = new AssetSubJobEvent();
    assetSubJobEvent.setFilePath(List.of("Test.png"));
    assetSubJobEvent.setParentJobId(UUID.randomUUID().toString());
    assetSubJobEvent.setSubJobId(UUID.randomUUID().toString());
    assetSubJobEvent.setSubJobType(AssetCategory.METADATA);
    assetSubJobEvent.setJobName("TestJobName");
    assetSubJobEvent.setMetaDataFileUrl("12349");
    assetSubJobEvent.setUserRoles(List.of("CollManagerPrimaryAutomotiveRole"));

    kafkaProducer.sendMessage(TEST_TOPIC, assetSubJobEvent);
    try (var consumer = configureConsumer()){
      ConsumerRecord<String, AssetSubJobEvent> record = KafkaTestUtils.getSingleRecord(consumer, TEST_TOPIC);
      AssetSubJobEvent receivedAssetSubJobEvent = objectMapper.readValue(record.value().toString(), AssetSubJobEvent.class);

      assertEquals(assetSubJobEvent.getFilePath(), receivedAssetSubJobEvent.getFilePath(), "File name is not equal");
      assertEquals(assetSubJobEvent.getParentJobId(), receivedAssetSubJobEvent.getParentJobId(), "Parent job id is not equal");
      assertEquals(assetSubJobEvent.getSubJobId(), receivedAssetSubJobEvent.getSubJobId(), "Sub job id is not equal");
      assertEquals(assetSubJobEvent.getSubJobType(), receivedAssetSubJobEvent.getSubJobType(), "Sub job type id is not equal");
      assertEquals(assetSubJobEvent.getUserRoles(), receivedAssetSubJobEvent.getUserRoles(), "User roles are not equal");
    }
  }

  private Consumer<String, String> configureConsumerProp() {
    Map<String, Object> consumerProps =
        KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    Consumer<String, String> consumer =
        new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
    consumer.subscribe(Collections.singleton(TEST_TOPIC));
    return consumer;
  }
  private Consumer<String, AssetSubJobEvent> configureConsumer() {
    Map<String, Object> consumerProps =
            KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    consumerProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
    consumerProps.put("specific.avro.reader", true);
    Consumer<String, AssetSubJobEvent> consumer =
            new DefaultKafkaConsumerFactory<String, AssetSubJobEvent>(consumerProps).createConsumer();
    consumer.subscribe(Collections.singleton(TEST_TOPIC));
    return consumer;
  }

  public ProducerFactory<String, AssetSubJobEvent> configureKafkaTemplateAssetSubJob() {
    Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    producerProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
    return new DefaultKafkaProducerFactory<>(producerProps);
  }
  private Producer<String, String> configureProducer() {
    Map<String, Object> producerProps =
        new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
    return new DefaultKafkaProducerFactory<>(
            producerProps, new StringSerializer(), new StringSerializer())
        .createProducer();
  }
}

