package com.xperi.datamover.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.Map;

/** This is a DTO class for creating Job */
@Data
@EqualsAndHashCode(callSuper=true)
public class MetadataJobRequestDto extends BaseAssetJobDto {

  // Name for the job
  @NotNull(message = "Job name should not be null or empty")
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

  @Min(value = 1, message = "No. of metadata should not be less than 1")
  private long noOfMetadata;
}
