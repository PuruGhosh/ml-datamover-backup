package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetJobStatus;
import lombok.Data;

import java.util.Date;

/** This class is a base class for different job & sub job dto */
@Data
class BaseAssetJobDto {

  // Job Id
  protected String id;

  // Job creation timestamp
  protected Date createdAt;

  // Job creation timestamp
  protected String createdBy;

  // Job status
  protected AssetJobStatus status;

  // Job data modification timestamp
  protected Date updatedAt;

  // To be used for any error message or any note for the job in future
  protected String message;
}
