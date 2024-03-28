package com.xperi.datamover.util;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.model.AssetEvent;
import com.xperi.datamover.model.minio.*;
import com.xperi.datamover.model.minio.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** This class contains different utility methods for Kafka services. */
@Slf4j
@Component
public class KafkaUtil {

  private static final String PRINCIPAL_ID = "principalId";
  private static final String PARENT_JOB_ID = "X-Amz-Meta-Parentjobid";
  private static final String SUB_JOB_ID = "X-Amz-Meta-Subjobid";
  private static final String SUB_JOB_TYPE = "X-Amz-Meta-Subjobtype";
  private static final String METADATA_FILE_URL = "X-Amz-Meta-Metadatafileurl";
  private static final String JOB_NAME = "X-Amz-Meta-Jobname";

  /**
   * This method parses Minio event string & extract necessary data.
   *
   * @param event - Received from Minio event listener
   * @return Parsed minio event object
   */
  public AssetEvent parseMinioEvent(BucketEvent event) {
    try {
      AssetEvent assetEvent = new AssetEvent();

      if (!CollectionUtils.isEmpty(event.getRecords())) {
        Record record = event.getRecords().get(0);
        S3 s3 = record.getS3();
        Bucket bucket = s3.getBucket();
        AssetObject assetObject = s3.getObject();

        assetEvent.setBucketName(bucket.getName());
        assetEvent.setKey(event.getKey());
        assetEvent.setFileName(assetObject.getKey());
        assetEvent.setAssetSize(assetObject.getSize());
        assetEvent.setContentType(assetObject.getContentType());
        assetEvent.setUserMetadata(assetObject.getUserMetadata());
        assetEvent.setVersionId(assetObject.getVersionId());
        assetEvent.setOwnerId(bucket.getOwnerIdentity().get(PRINCIPAL_ID));
        assetEvent.setParentJobId(assetObject.getUserMetadata().get(PARENT_JOB_ID));
        assetEvent.setSubJobId(assetObject.getUserMetadata().get(SUB_JOB_ID));
        if (assetObject.getUserMetadata().containsKey(SUB_JOB_TYPE)) {
          assetEvent.setSubJobType(
              AssetCategory.valueOf(assetObject.getUserMetadata().get(SUB_JOB_TYPE)));
        }
        assetEvent.setMetaDataFileUrl(assetObject.getUserMetadata().get(METADATA_FILE_URL));
        assetEvent.setJobName(assetObject.getUserMetadata().get(JOB_NAME));
      }
      return assetEvent;
    } catch (Exception exception) {
      log.error("Error in parsing Minio BucketEvent {}", exception.getMessage(), exception);
      throw exception;
    }
  }
}
