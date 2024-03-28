package com.xperi.datamover.dto;

import com.xperi.datamover.dto.metadata.AssetMetadata;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateBulkMetadataRequestDto {
    @NotBlank(message = "Asset Job Id is required")
    private UUID parentJobId;
    @NotEmpty(message = "Asset metadata are required")
    private List<AssetMetadata> assetMetadata;
}
