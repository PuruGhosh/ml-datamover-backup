package com.xperi.datamover.mapper;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.util.AssetSubJobMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetSubJobMapperUT {

  private final AssetSubJobMapper assetSubJobMapper = Mappers.getMapper(AssetSubJobMapper.class);

  @Test
  @DisplayName("Testing Sub Job Dto to Sub job Entity Mapper")
  public void testSubJobDtoToSubJobEntityMapper() {
    AssetSubJobDto assetSubJobDto = new AssetSubJobDto();
    Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", "testValue");

    assetSubJobDto.setParentJobId(UUID.randomUUID().toString());
    assetSubJobDto.setFileName(List.of("test"));
    assetSubJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobDto.setType(AssetCategory.ASSET);
    // assetSubJobDto.setMetaData(metaData);

    AssetSubJobEntity assetSubJobEntity = assetSubJobMapper.toAssetSubJobEntity(assetSubJobDto);
    assertEquals(
        assetSubJobDto.getParentJobId(),
        assetSubJobEntity.getParentJobId().toString(),
        "Parent job id is not equal");
    assertEquals(
        assetSubJobDto.getFileName(), assetSubJobEntity.getFileName(), "File name is not equal");
    assertEquals(assetSubJobDto.getStatus(), assetSubJobEntity.getStatus(), "Status is not equal");
    assertEquals(assetSubJobDto.getType(), assetSubJobEntity.getType(), "File type is not equal");
    //assertEquals(assetSubJobDto.getMetaData(), assetSubJobEntity.getMetaData(), "Meta data are not equal");
  }
}
