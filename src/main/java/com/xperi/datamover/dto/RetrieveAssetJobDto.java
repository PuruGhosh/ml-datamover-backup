package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/** This is a DTO class to retrieve job details and count of status for subJobs */
@Data
public class RetrieveAssetJobDto {
  private String id;
  private String ownerId;
  private Date createdAt;
  private AssetCategory jobType = AssetCategory.ASSET;
  private AssetJobStatus status;
  private String jobName;
  private Map<AssetJobStatus, Long> subJobStatus;
  private Map<AssetJobStatusAccumulator, Long> subJobStatusAccumulator;
  private String description;
  private String folder;
  private boolean initialMetadataFile;
}
