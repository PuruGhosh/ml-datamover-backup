package com.xperi.datamover.util;

import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.dto.MetadataJobRequestDto;
import com.xperi.datamover.dto.RetrieveAssetJobDto;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.exception.DataMappingException;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.File;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

/** Mapper class to map Asset Job entity and dto classes */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AssetJobMapper {

  @Named("uuidToString")
  static String uuidToString(UUID id) {
    return id == null ? null : id.toString();
  }

  @Named("stringToUuid")
  static UUID stringToUuid(String id) {
    return id == null ? null : UUID.fromString(id);
  }

  @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
  AssetJobEntity toJobEntity(AssetJobDto jobDto) throws DataMappingException;

  @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
  AssetJobEntity toJobEntity(MetadataJobRequestDto jobDto) throws DataMappingException;

  @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
  AssetJobDto fromJobEntity(AssetJobEntity assetJobEntity) throws DataMappingException;

  @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
  RetrieveAssetJobDto retrieveAssetJobDtoFromJobEntity(AssetJobEntity assetJobEntity)
      throws DataMappingException;

  List<RetrieveAssetJobDto> toRetrieveAssetJobDtoList(List<AssetJobEntity> assetJobEntity)
      throws DataMappingException;

  @Mapping(source = "asset_uuid", target = "assetUuid")
  @Mapping(source = "meta_file_name", target = "metaFileName")
  @Mapping(source = "filePartMetadata.file_name", target = "filePartMetadata.fileName")
  @Mapping(source = "filePartMetadata.meta_file_name", target = "filePartMetadata.metaFileName")
  @Mapping(source = "filePartMetadata.metadata_type", target = "filePartMetadata.metadataType",defaultValue = "UNKNOWN")
  @Mapping(source = "storePartMetadata.eTag", target = "storePartMetadata.ETag")
  AssetMetadata toAssetMetadataSchema(com.xperi.datamover.dto.metadata.AssetMetadata assetMetadata)
      throws DataMappingException;

  @Mapping(source = "file_name", target = "fileName")
  @Mapping(source = "meta_file_name", target = "metaFileName")
  @Mapping(source = "metadata_type", target = "metadataType", defaultValue = "UNKNOWN")
  File toFileSchema(com.xperi.datamover.dto.metadata.File file) throws DataMappingException;
}
