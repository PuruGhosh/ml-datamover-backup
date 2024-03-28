package com.xperi.datamover.model.minio;

import lombok.Data;

/**
 * This class contains the properties for Minio S3 object. These properties are used during parsing
 * Minio event.
 */
@Data
public class S3 {

  private Bucket bucket;
  private AssetObject object;
}
