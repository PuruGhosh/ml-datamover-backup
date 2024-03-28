package com.xperi.datamover.model.minio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * This class contains the properties for Minio BucketEvent object. These properties are used during
 * parsing Minio event.
 */
@Data
public class BucketEvent {

  @JsonProperty("Records")
  private List<Record> Records;

  @JsonProperty("Key")
  private String Key;

  private String subJobId;
  private String message;
  private boolean error;
}
