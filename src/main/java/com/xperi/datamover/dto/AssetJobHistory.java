package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetJobStatus;
import lombok.Data;

import java.util.Date;

@Data
public class AssetJobHistory {
  private AssetJobStatus status;
  private Date modifiedDate;
}
