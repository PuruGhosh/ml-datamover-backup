/** */
package com.xperi.datamover.task;

import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.exception.DataMoverException;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.File;
import com.xperi.schema.metadata.Operations;
import com.xperi.schema.metadata.Store;
import com.xperi.schema.subjobevent.AssetCategory;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.InterruptException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This the runnable thread which is called in an async mannter
 * It does two operations based on the caller. 1/create mongo entry 2/sends to Kafka
 */
@Slf4j
@ToString
@AllArgsConstructor
public class AssetTask implements Runnable {

  private final AssetSubJobService assetSubJobService;
  private final AssetSubJobDto assetSubJobDto;
  private final String metadataFileName;
  private final String minioMetadataFileURL;
  final Map<String, String> uiPartMetadata;
  private final List<String> userRoles;
  private final AssetMetadata assetMetadata;

  @Override
  public void run() {
    try{
      // create the data in mongoDB
      if (StringUtils.isEmpty(assetSubJobDto.getId())){
        final String assetSubJobId = assetSubJobService.create(assetSubJobDto);
        assetSubJobDto.setId(assetSubJobId);
      }
      //create this only if necessary checks are passed.
      AssetSubJobEvent assetSubJobEvent;

      /* if there is no metadata file uploaded with the asset files, then let us send a message
      to Kafka for individual asset files Or  if we already get a response that metadata file has been uploader or
      if the type is metadata let us send it to Kafka
      */

      switch (assetSubJobDto.getType()){
        case METADATA -> assetSubJobEvent = createAssetSubJobEvent(assetSubJobDto, userRoles);
        case ASSET -> {
          if(StringUtils.isNotEmpty(metadataFileName)){
            if(assetMetadata!=null){
              assetMetadata.setAssetUuid(assetSubJobDto.getId());
              assetMetadata.setSubJobId(assetSubJobDto.getId());
              assetMetadata.setStorePartMetadata(createEmptyStorePartMetadata());
              assetMetadata.setUserRoles(userRoles);
              assetSubJobService.sendAssetMetadataMessageToKafka(assetMetadata);
              log.info("AssetMetadata details sent to Kafka for Indexing. Details - {} , for sub-job id: {}",assetMetadata, assetSubJobDto.getId());
            } else {
              throw new DataMoverException("Failed to collect AssetMetadata for subIobId - %s".formatted(assetSubJobDto.getId()));
            }
          } else {
            // sendAssetMetadataForUiMetadata();
            throw new DataMoverException("Metadata file name is not present for subIobId - %s".formatted(assetSubJobDto.getId()));
          }
          assetSubJobEvent=createAssetSubJobEvent(assetSubJobDto, userRoles);
        }
        default -> throw new RuntimeException("Unknown asset type "+assetSubJobDto.getType());
      }

      //To produce message in ml-asset for uploading the file to minIO
      assetSubJobService.sendAssetSubJobMessageToKafka(assetSubJobEvent);
      log.info("FINISHED - Sent Asset SubJob Message to Kafka to upload the asset in minIO. AssetSubJobEvent details - {}", assetSubJobEvent);

    } catch(InterruptException e){
      log.error("Thread interrupted while processing assetSubJob: {}",assetSubJobDto.getId());
    }
  }

  private AssetSubJobEvent createAssetSubJobEvent(AssetSubJobDto assetSubJobDto, List<String> userRoles){
    final var assetSubJobEvent = new AssetSubJobEvent();
    assetSubJobEvent.setParentJobId(assetSubJobDto.getParentJobId());
    assetSubJobEvent.setSubJobId(assetSubJobDto.getId());
    assetSubJobEvent.setSubJobType(AssetCategory.valueOf(assetSubJobDto.getType().name()));
    assetSubJobEvent.setFilePath(assetSubJobDto.getFilePath());
    assetSubJobEvent.setJobName(assetSubJobDto.getJobName());
    assetSubJobEvent.setMetaDataFileUrl(
            StringUtils.isEmpty(minioMetadataFileURL)?"":minioMetadataFileURL
    );

    assetSubJobEvent.setUserRoles(userRoles);
    return assetSubJobEvent;
  }

  private void sendAssetMetadataForUiMetadata(){
    var assetMetadata = createEmptyAssetMetadata();
    assetMetadata.setUiPartMetadata(uiPartMetadata);
    assetMetadata.setAssetUuid(assetSubJobDto.getId());
    assetMetadata.setSubJobId(assetSubJobDto.getId());
    assetMetadata.setOperation(Operations.CREATE);
    assetMetadata.setUserRoles(userRoles);
    log.debug("Creating AssetMetadata AVRO message to send only UI Part Metadata: {}", assetMetadata);
    assetSubJobService.sendAssetMetadataMessageToKafka(assetMetadata);
  }

  private AssetMetadata createEmptyAssetMetadata(){
    var file = new File();
    file.setMetaFileName("");
    file.setFileName(List.of());
    file.setOwner("");

    var assetMetadata = new AssetMetadata();
    assetMetadata.setFilePartMetadata(file);
    assetMetadata.setStorePartMetadata(createEmptyStorePartMetadata());
    assetMetadata.setMetaFileName("");
    assetMetadata.setParameters("");
    assetMetadata.setContentAssetMetadata("");
    return assetMetadata;
  }



  public String getAssetSubJobId() {
    return assetSubJobDto.getId();
  }

  public Store createEmptyStorePartMetadata(){
    var storePartMetadata = new Store();
    storePartMetadata.setContentType("");
    storePartMetadata.setKey("");
    storePartMetadata.setUserMetadata(Collections.emptyMap());
    storePartMetadata.setSize(0L);
    storePartMetadata.setSequencer("");
    storePartMetadata.setETag("");
    storePartMetadata.setVersionId("");
    return storePartMetadata;
  }
}
