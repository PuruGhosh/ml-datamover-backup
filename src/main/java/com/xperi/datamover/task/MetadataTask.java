/** */
package com.xperi.datamover.task;

import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.Operations;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InterruptException;

import java.util.List;

/**
 * This the runnable thread which is called in an async manner It does two operations based on the
 * caller. 1/create mongo entry 2/sends to Kafka
 */
@Slf4j
@ToString
@AllArgsConstructor
public class MetadataTask implements Runnable {

  private final AssetSubJobService assetSubJobService;
  private final AssetSubJobDto assetSubJobDto;
  private final AssetMetadata assetMetadata;
  private final List<String> userRoles;

  @Override
  public void run() {
    try {
      final String assetSubJobId = assetSubJobService.create(assetSubJobDto);
      assetSubJobDto.setId(assetSubJobId);
      assetMetadata.setOperation(Operations.UPDATE);
      assetMetadata.setSubJobId(assetSubJobDto.getId());
      assetMetadata.setUserRoles(userRoles);
      assetSubJobService.sendAssetMetadataMessageToKafka(assetMetadata);
      log.debug("FINISH - Save to database {}", assetSubJobDto);
    } catch (InterruptException e) {
      log.error("Thread interrupted while processing assetSubJob: {}" + assetSubJobDto.getId());
    }
  }

  public String getAssetSubJobId() {
    return assetSubJobDto.getId();
  }
}
