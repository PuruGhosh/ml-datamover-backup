package com.xperi.datamover.dto.metadata;

import lombok.Data;

@Data
public class Store {
  private long size;
  private String contentType;
  private java.util.Map<String, String> userMetadata;
  private String versionId;
  private String key;
  private String eTag;
  private String sequencer;
  public void seteTag (String ETag) {
    this.eTag = ETag;
  }
  public String geteTag () {
    return eTag;
  }
}
