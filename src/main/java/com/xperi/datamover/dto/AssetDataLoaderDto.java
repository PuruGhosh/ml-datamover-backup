package com.xperi.datamover.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/** This is a DTO class for getting all asset sub-jobs using list of asset ids */
@Data
public class AssetDataLoaderDto {

  @NotEmpty(message = "Assets ids are required")
  private List<String> assetIds;
}
