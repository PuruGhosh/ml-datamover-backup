package com.xperi.datamover.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.dto.*;
import com.xperi.datamover.dto.metadata.*;
import com.xperi.datamover.model.minio.AssetObject;
import com.xperi.datamover.model.minio.Bucket;
import com.xperi.datamover.model.minio.BucketEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class DataMoverTestUtil {

  public static final String TEMP_ASSET_FILE = "dummy.png";
  public static final String FILE_DOT_SEPARATOR = ".";
  public static final String TEMP_METADATA_FILE = "metadata.json";
  private static final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  /**
   * util method to return the AssetJobDto
   *
   * @return AssetJobDto
   */
  public static AssetJobDto createAssetJobDto() {
    return createAssetJobDto(TEMP_ASSET_FILE);
  }

  public static AssetJobDto createAssetJobDto(String filePath) {
    final var assetJobDto = new AssetJobDto();
    final Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", "testValue");
    assetJobDto.setOwnerId("testOwner");
    assetJobDto.setMessage("testMessage");
    assetJobDto.setJobName("testGroup");
    assetJobDto.setDescription("testDescription");
    assetJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobDto.setAssetLocationType(AssetLocationType.FILE);
    final var fileNames = Collections.singletonList(filePath);
    assetJobDto.setLocations(fileNames);
    assetJobDto.setMetaData(metaData);

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, 1L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 0L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);

    assetJobDto.setSubJobStatusAccumulator(subJobStatusAccumulator);

    return assetJobDto;
  }

  public static RetrieveAssetJobDto createRetrieveAssetJobDto() {
    final var retrieveAssetJobDto = new RetrieveAssetJobDto();
    final Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", "testValue");
    retrieveAssetJobDto.setOwnerId("testOwner");
    retrieveAssetJobDto.setJobName("testGroup");
    retrieveAssetJobDto.setDescription("testDescription");
    retrieveAssetJobDto.setStatus(AssetJobStatus.IN_PROGRESS);

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, -1L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 0L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);

    retrieveAssetJobDto.setSubJobStatusAccumulator(subJobStatusAccumulator);

    return retrieveAssetJobDto;
  }

  /**
   * util method to return the AssetSubJobDto
   *
   * @return AssetSubJobDto
   */
  public static AssetSubJobDto createAssetSubJobDto() {
    // dummy asset sub Job Dto object for retrieval
    final AssetSubJobDto assetSubJobDto = new AssetSubJobDto();
    Map<String, String> metaData = new HashMap<String, String>();
    metaData.put("testKey", "testValue");
    assetSubJobDto.setId(UUID.randomUUID().toString());
    assetSubJobDto.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobDto.setParentJobId(UUID.randomUUID().toString());
    assetSubJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobDto.setFileName(List.of(TEMP_ASSET_FILE));
    assetSubJobDto.setType(AssetCategory.ASSET);
    // assetSubJobDto.setMetaData(metaData);
    return assetSubJobDto;
  }

  public static BucketEvent createBucketEvent(
      String fileName, String jobName, String parentJobId, String subJobId) {
    try {
      URL resource =
          DataMoverTestUtil.class.getClassLoader().getResource("minio_sample_event.json");
      byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
      var bucketEventTemplate = objectMapper.readValue(resource, BucketEvent.class);

      bucketEventTemplate.setKey("/assets/" + fileName);
      Bucket bucket = bucketEventTemplate.getRecords().get(0).getS3().getBucket();
      AssetObject assetObject = bucketEventTemplate.getRecords().get(0).getS3().getObject();
      assetObject.getUserMetadata().put("X-Amz-Meta-Subjobid", subJobId);
      assetObject.getUserMetadata().put("X-Amz-Meta-Parentjobid", parentJobId);
      assetObject.getUserMetadata().put("X-Amz-Meta-Jobname", jobName);
      assetObject.getUserMetadata().put("X-Amz-Meta-Subjobtype", AssetCategory.ASSET.name());

      return bucketEventTemplate;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static Path getFileFromClasspath(String folderName) {
    try {
      URL resource = DataMoverTestUtil.class.getClassLoader().getResource(folderName);
      return Path.of(resource.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static MetadataJobRequestDto createMetadataJobRequestDto() {
    final MetadataJobRequestDto metadataJobRequestDto = new MetadataJobRequestDto();
    metadataJobRequestDto.setId(UUID.randomUUID().toString());
    metadataJobRequestDto.setJobName("testGroup");
    metadataJobRequestDto.setDescription("testDescription");
    metadataJobRequestDto.setNoOfMetadata(5);
    return metadataJobRequestDto;
  }

  public static UpdateBulkMetadataRequestDto createUpdateBulkMetadataRequestDto() {
    final UpdateBulkMetadataRequestDto updateBulkMetadataRequestDto =
        new UpdateBulkMetadataRequestDto();
    AssetMetadata assetMetadata = new AssetMetadata();
    Store store = new Store();
    store.setKey("4612075317_91eefff68c_n.jpg");
    store.setSize(57547);
    store.setContentType("image/jpeg");
    store.setVersionId("acce6b85-d21a-4840-9774-0cb7337ae8e6");
    store.setSequencer("12345678");
    store.seteTag("11ee");
    Map<String, String> usermetadatamap = new HashMap<>();
    usermetadatamap.put("X-Amz-Meta-Jobname", "mldp-264-test-1");
    store.setUserMetadata(usermetadatamap);
    File file = new File();
    file.setFile_name(List.of("4612075317_91eefff68c_n.jpg"));
    file.setOwner("testOwner");
    file.setMetadata("{\"label\":\"roses\"}");
    file.setMeta_file_name("labels.json");
    file.setMetadata_type(MetadataType.JSON);
    assetMetadata.setUiPartMetadata(usermetadatamap);
    assetMetadata.setFilePartMetadata(file);
    assetMetadata.setAsset_uuid("e0c54439-e011-4b5a-945f-c6058444e153");
    assetMetadata.setOperation(Operations.UPDATE);
    assetMetadata.setMeta_file_name("labels.json");
    assetMetadata.setParameters("{}");
    assetMetadata.setSubJobId("e0c54439-e011-4b5a-945f-c6058444e154");
    assetMetadata.setStorePartMetadata(store);
    updateBulkMetadataRequestDto.setParentJobId(UUID.randomUUID());
    updateBulkMetadataRequestDto.setAssetMetadata(List.of(assetMetadata));
    return updateBulkMetadataRequestDto;
  }

  public static void copyFiles(Path source, Path target) throws IOException {
    if (Files.isDirectory(source)) {
      if (Files.notExists(target)) {
        Files.createDirectories(target);
      }
      try (Stream<Path> paths = Files.list(source)) {
        // recursive loop
        paths.forEach(
            p -> {
              try {
                var filename = source.relativize(p).toString();
                copyFiles(p, target.resolve(filename));
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });
      }
    } else {
      Files.copy(source, target);
    }
  }
}
