package com.xperi.datamover.model.minio;

import lombok.Data;

import java.util.Map;

/**
 * This class contains the properties for Minio asset object. These properties are used during
 * parsing Minio event.
 */
@Data
public class AssetObject {
  private long size;
  private String contentType;
  private Map<String, String> userMetadata;
  private String versionId;
  private String key;
  private String eTag;
  private String sequencer;
}
