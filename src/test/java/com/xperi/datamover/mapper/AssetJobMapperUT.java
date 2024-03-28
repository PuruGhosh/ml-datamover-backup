package com.xperi.datamover.mapper;

import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.util.AssetJobMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** This class is used for unit testing of Mapper */
public class AssetJobMapperUT {

  private final AssetJobMapper assetJobMapper = Mappers.getMapper(AssetJobMapper.class);

  @Test
  @DisplayName("Testing Job Dto to job Entity Mapper")
  public void testJobDtoToJobEntityMapper() {
    AssetJobDto assetJobDto = new AssetJobDto();
    Map<String, String> metaData = new HashMap<>();
    List<String> fileNames = new ArrayList<>();

    fileNames.add("file1");
    fileNames.add("file2");
    metaData.put("testKey", "testValue");

    assetJobDto.setOwnerId("testOwner");
    assetJobDto.setMessage("testMessage");
    assetJobDto.setJobName("testJob");
    assetJobDto.setDescription("testDescription");
    assetJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobDto.setAssetLocationType(AssetLocationType.FILE);
    assetJobDto.setLocations(fileNames);
    assetJobDto.setMetaData(metaData);

    AssetJobEntity assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);

    assertEquals(assetJobDto.getOwnerId(), assetJobEntity.getOwnerId(), "Owner id is not equal");
    assertEquals(assetJobDto.getMessage(), assetJobEntity.getMessage(), "Message is not equal");
    assertEquals(assetJobDto.getJobName(), assetJobEntity.getJobName(), "Job name is not equal");
    assertEquals(
        assetJobDto.getDescription(), assetJobEntity.getDescription(), "Description is not equal");
    assertEquals(assetJobDto.getStatus(), assetJobEntity.getStatus(), "Status is not equal");
    assertEquals(
        assetJobDto.getAssetLocationType(),
        assetJobEntity.getAssetLocationType(),
        "Location type is not equal");
    assertEquals(
        assetJobDto.getLocations(), assetJobEntity.getLocations(), "Locations are not equal");
    assertEquals(
        assetJobDto.getMetaData(), assetJobEntity.getMetaData(), "Meta data are not equal");
  }

  @Test
  @DisplayName("Testing Job Entity to job Dto Mapper")
  public void testJobEntityToJobDtoMapper() {
    AssetJobEntity assetJobEntity = new AssetJobEntity();
    Map<String, String> metaData = new HashMap<>();
    List<String> fileNames = new ArrayList<>();

    fileNames.add("file1");
    fileNames.add("file2");
    metaData.put("testKey", "testValue");

    assetJobEntity.setId(UUID.randomUUID());
    assetJobEntity.setOwnerId("testOwner");
    assetJobEntity.setMessage("testMessage");
    assetJobEntity.setJobName("testJobName");
    assetJobEntity.setDescription("testDescription");
    assetJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobEntity.setAssetLocationType(AssetLocationType.FILE);
    assetJobEntity.setLocations(fileNames);
    assetJobEntity.setMetaData(metaData);

    AssetJobDto assetJobDto = assetJobMapper.fromJobEntity(assetJobEntity);

    assertEquals(assetJobEntity.getOwnerId(), assetJobDto.getOwnerId(), "Owner id is not equal");
    assertEquals(assetJobEntity.getMessage(), assetJobDto.getMessage(), "Message is not equal");
    assertEquals(assetJobEntity.getJobName(), assetJobDto.getJobName(), "Job name is not equal");
    assertEquals(
        assetJobEntity.getDescription(), assetJobDto.getDescription(), "Description is not equal");
    assertEquals(assetJobEntity.getStatus(), assetJobDto.getStatus(), "Status is not equal");
    assertEquals(
        assetJobEntity.getAssetLocationType(),
        assetJobDto.getAssetLocationType(),
        "Location type is not equal");
    assertEquals(
        assetJobEntity.getLocations(), assetJobDto.getLocations(), "Locations are not equal");
    assertEquals(
        assetJobEntity.getMetaData(), assetJobDto.getMetaData(), "Meta data are not equal");
  }
}
