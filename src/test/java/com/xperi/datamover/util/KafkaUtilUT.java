package com.xperi.datamover.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.model.AssetEvent;
import com.xperi.datamover.model.minio.BucketEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaUtilUT {
  private final KafkaUtil kafkaUtil = new KafkaUtil();
  private final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  @DisplayName("Unit Testing of Minio BucketEvent Json parser")
  public void testMinioEventParser() throws IOException, URISyntaxException {
    URL resource = getClass().getClassLoader().getResource("minio_sample_event.json");
    byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
    String minioEventString = new String(bytes);

    BucketEvent bucketEvent = objectMapper.readValue(minioEventString, BucketEvent.class);

    AssetEvent assetEvent = kafkaUtil.parseMinioEvent(bucketEvent);

    assertEquals("assets", assetEvent.getBucketName(), "Bucket name is not equal");
    assertEquals("assets/test.png", assetEvent.getKey(), "Key name is not equal");
    assertEquals(368, assetEvent.getAssetSize(), "Asset size is not equal");
    assertEquals("image/png", assetEvent.getContentType(), "Asset content type is not equal");
    assertEquals("1", assetEvent.getVersionId(), "Asset version id is not equal");
    assertEquals("admin", assetEvent.getOwnerId(), "Owner id is not equal");
    assertEquals("test.png", assetEvent.getFileName());
  }
}
