package com.xperi.datamover.entity;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.dto.AssetJobHistory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

/** This is Asset Sub-Job document class */
@Document(collection = "assetSubJobs")
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetSubJobEntity extends BaseEntity {

  @Id private UUID id;

  // Parent Job id
  private UUID parentJobId;

  // Sub job status
  private AssetJobStatus status;

  // File path for an asset or a metadata file
  private List<String> filePath;

  // Asset url
  private String url;

  // To be used for any error message or any note for the job in future
  private String message;

  // File type (Asset or Metadata)
  private AssetCategory type;

  // To track the asset sub job history
  private List<AssetJobHistory> history;

  // Metadata file url
  private String metaDataFileUrl;

  // Minio generated version id for asset
  private String minioVersionId;

  // Semantic version id for asset
  private int semanticVersionId;

  // Asset or metadata file name
  private List<String> fileName;

  // Name for the job
  private String jobName;

  // Enabled, when the asset will be successfully uploaded in MinIO
  private boolean stored;

  // Enabled, when the asset will be successfully indexed in Elasticsearch
  private boolean indexed;
}
