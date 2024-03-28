package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** This is a DTO class for creating Sub-Job */
@Data
@EqualsAndHashCode(callSuper=true)
public class AssetSubJobDto extends BaseAssetJobDto {

  // Parent Job id
  private String parentJobId;

  // File path for an asset or a metadata file
  private List<String> filePath;

  // Name for the job
  private String jobName;

  // Asset url
  private String url;

  // File type (Asset or Metadata)
  private AssetCategory type;

  // Asset metadata
  // private Map<String, String> metaData;

  // Metadata file url
  private String metaDataFileUrl;

  //Minio generated version id for asset
  private String minioVersionId;

  // Semantic version id for asset
  private int semanticVersionId;

  // Asset or metadata file name
  private List<String> fileName;

  // Enabled, when the asset will be successfully uploaded in MinIO
  private boolean stored;

  // Enabled, when the asset will be successfully indexed in Elasticsearch
  private boolean indexed;
}
