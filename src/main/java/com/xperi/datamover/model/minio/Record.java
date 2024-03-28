package com.xperi.datamover.model.minio;

import lombok.Data;

/**
 * This class contains the properties for Minio Record object. These properties are used during
 * parsing Minio event.
 */
@Data
public class Record {

  private S3 s3;
}
