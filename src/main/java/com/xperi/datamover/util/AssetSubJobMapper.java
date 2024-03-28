package com.xperi.datamover.util;

import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.exception.DataMappingException;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

/** Mapper class to map Asset Sub-Job entity and dto classes */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AssetSubJobMapper {

  @Named("uuidToString")
  static String uuidToString(UUID id) {
    return id == null ? null : id.toString();
  }

  @Named("stringToUuid")
  static UUID stringToUuid(String id) {
    return id == null ? null : UUID.fromString(id);
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "parentJobId", target = "parentJobId", qualifiedByName = "stringToUuid")
  AssetSubJobEntity toAssetSubJobEntity(AssetSubJobDto assetSubJobDto) throws DataMappingException;

  @Mapping(source = "assetSubJobEntity.id", target = "id", qualifiedByName = "uuidToString")
  @Mapping(
      source = "assetSubJobEntity.parentJobId",
      target = "parentJobId",
      qualifiedByName = "uuidToString")
  @Mapping(source = "parentJobName", target = "jobName")
  AssetSubJobDto toAssetSubJobDto(AssetSubJobEntity assetSubJobEntity, String parentJobName)
      throws DataMappingException;

  @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
  @Mapping(source = "parentJobId", target = "parentJobId", qualifiedByName = "uuidToString")
  AssetSubJobDto toAssetSubJobDto(AssetSubJobEntity assetSubJobEntity) throws DataMappingException;

  List<AssetSubJobDto> toAssetSubJobDtoList(List<AssetSubJobEntity> assetSubJobEntity)
      throws DataMappingException;

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentJobId", ignore = true)
  void updateAssetSubJobFromDto(
      AssetSubJobDto assetSubJobDto, @MappingTarget AssetSubJobEntity entity)
      throws DataMappingException;
}
