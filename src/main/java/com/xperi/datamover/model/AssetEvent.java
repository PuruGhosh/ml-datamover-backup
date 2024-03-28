package com.xperi.datamover.model;


import com.xperi.datamover.constants.AssetCategory;
import lombok.Data;

import java.util.Map;

/** This class contains the properties for Parsed Minio BucketEvent. */
@Data
public class AssetEvent {

  private String bucketName;
  private String key;
  private long assetSize;
  private String contentType;
  private Map<String, String> userMetadata;
  private String versionId;
  private String ownerId;
  private String parentJobId;
  private String subJobId;
  private String metaDataFileUrl;
  private AssetCategory subJobType;
  private String fileName;
  private String jobName;
}
