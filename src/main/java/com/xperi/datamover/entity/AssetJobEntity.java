package com.xperi.datamover.entity;

import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.constants.AssetCategory;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** This is Asset Job document class */
@Document(collection = "assetJobs")
@Data
public class AssetJobEntity {

  // Job id
  @Id private UUID id;

  // Owner of the job.
  private String ownerId;

  // Job creation timestamp
  private Date createdAt;

  // File type (Asset or Metadata)
  private AssetCategory jobType = AssetCategory.ASSET;

  // Job status
  private AssetJobStatus status;

  // Job data modification timestamp
  private Date updatedAt;

  // To be used for any error message or any note for the job in future
  private String message;

  // Name for the job
  private String jobName;

  // Description of the job
  private String description;

  // Assets' location type
  private AssetLocationType assetLocationType;

  // List of asset paths or folder path
  private List<String> locations;

  // Metadata file(Json only) path
  private String metaDataFileName;

  // Metadata as key value pair
  private Map<String, String> metaData;

  // subJobStatusAccumulator maintains the status count of the sub-jobs
  private Map<AssetJobStatusAccumulator, Long> subJobStatusAccumulator;

  // Folder name of the job
  private String folder;

  // Initial metadata file is ingested along with the asset
  private boolean initialMetadataFile;
}
