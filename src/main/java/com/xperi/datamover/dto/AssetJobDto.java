package com.xperi.datamover.dto;

import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.constants.AssetLocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/** This is a DTO class for creating Job */
@Data
@EqualsAndHashCode(callSuper=true)
public class AssetJobDto extends BaseAssetJobDto {

  // Owner of the job.
  private String ownerId;

  // Name for the job
  @Size(max = 256, message = "Job name can not be more than 256 alphanumeric")
  @Pattern.List({
          @Pattern(regexp = "(^[^\s].*)",
                  message = "Job name must not contain any whitespace at the beginning"),
          @Pattern(regexp = "(.*[^\s]$)",
                  message = "Job name must not contain any whitespace at the end"),
          @Pattern(regexp = "(?=.*[a-zA-Z]).+", message = "Job name must contain one alphabet")
  })
  private String jobName;

  // Description of the job
  @NotBlank(
          message =
                  "Job description must not be null and must contain at least one non-whitespace character")
  @Size(max = 2048, message = "Job description can not be more than 2048 alphanumeric")
  private String description;

  // Assets' location type
  private AssetLocationType assetLocationType;

  // List of asset paths or folder path
  @NotEmpty(message = "Assets locations are required")
  private List<String> locations;

  // Metadata file(Json only) path
  @Pattern(
      regexp = "^$|([a-zA-Z0-9\\ \\s_\\\\.\\-\\(\\):/])+.json$",
      message = "Meta data file should be json file")
  @NotEmpty(message = "metadata fileName should not be empty.")
  private String metaDataFileName;

  // Metadata as key value pair
  private Map<
          @Size(max = 256, message = "Metadata Key can not be more than 256 alphanumeric")
          @Pattern.List({
                  @Pattern(regexp = "(^[^\s].*)",
                          message = "Metadata key must not contain any whitespace at the beginning"),
                  @Pattern(regexp = "(.*[^\s]$)",
                          message = "Metadata key must not contain any whitespace at the end"),
                  @Pattern(regexp = "(?=.*[a-zA-Z]).+", message = "Metadata key must contain one alphabet")
          })
          String,
          @Size(max = 1024, message = "Metadata value can not be more than 1024 alphanumeric")
          @Pattern.List({
                  @Pattern(regexp = "(^[^\s].*)",
                          message = "Metadata value must not contain any whitespace at the beginning"),
                  @Pattern(regexp = "(.*[^\s]$)",
                          message = "Metadata value must not contain any whitespace at the end"),
                  @Pattern(regexp = "(?=.*[a-zA-Z]).+", message = "Metadata value must contain one alphabet")
          })
          String>
      metaData;

  // subJobStatusAccumulator maintains the status count of the sub-jobs
  private Map<AssetJobStatusAccumulator, Long> subJobStatusAccumulator;
}
