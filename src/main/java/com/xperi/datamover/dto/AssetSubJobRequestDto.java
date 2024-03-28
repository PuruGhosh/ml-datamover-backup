package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetJobStatus;
import lombok.Data;

@Data
public class AssetSubJobRequestDto {

  // Status of the asset sub-jobs to find
  private AssetJobStatus status;

  // Page number to retrieve associations
  private int pageNo;
}
