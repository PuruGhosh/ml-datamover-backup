package com.xperi.datamover.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.model.minio.BucketEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Value deserializer class for bucket event message consumer */
@Slf4j
public class BucketEventDeserializer implements Deserializer<BucketEvent> {

    private final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public BucketEvent deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), BucketEvent.class);
        } catch (Exception e) {
            log.error("Unable to deserialize message {}", data, e);
            return null;
        }
    }

    @Override
    public void close() {}
}