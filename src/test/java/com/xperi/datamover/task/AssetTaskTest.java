package com.xperi.datamover.task;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.exception.DataMoverException;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.util.DataMoverTestUtil;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.File;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class AssetTaskTest {

  @Mock private AssetSubJobService assetSubJobService;

  @Test
  public void testAssetWithoutMetadataFile() throws InterruptedException {
    final UUID subJobId = UUID.randomUUID();
    String minioMetadataFileURL = null;
    String metadataFileName = null;
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    var subJobDto = DataMoverTestUtil.createAssetSubJobDto();
    subJobDto.setId(null);
    subJobDto.setMetaDataFileUrl(metadataFileName);

    when(assetSubJobService.create(any(AssetSubJobDto.class))).thenReturn(subJobId.toString());

    var assetTask =
        new AssetTask(
            assetSubJobService,
            subJobDto,
            metadataFileName,
            minioMetadataFileURL,
            null,
            userRoles,
            null);

    // If there is no metadata file then it will throw exception. We are asserting that exception
    // here.
    assertThrows(
        DataMoverException.class,
        () -> {
          assetTask.run();
        });
  }

  @Test
  public void testAssetWithMetadataFile() throws InterruptedException {
    final UUID subJobId = UUID.randomUUID();
    String minioMetadataFileURL = null;
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    String metadataFileName = "some-metadata-file.json";
    var path = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-v2");
    var subJobDto = DataMoverTestUtil.createAssetSubJobDto();
    subJobDto.setId(null);
    subJobDto.setMetaDataFileUrl(metadataFileName);
    subJobDto.setFilePath(List.of(path.resolve("fake-image-1.jpg").toAbsolutePath().toString()));
    subJobDto.setFileName(List.of("fake-image-1.jpg"));

    when(assetSubJobService.create(any(AssetSubJobDto.class))).thenReturn(subJobId.toString());

    var assetMetadata = createNewAssetMetadata();

    var assetTask =
        new AssetTask(
            assetSubJobService,
            subJobDto,
            metadataFileName,
            minioMetadataFileURL,
            null,
            userRoles,
            assetMetadata);
    assetTask.run();

    verify(assetSubJobService, times(1)).create(any(AssetSubJobDto.class));
    verify(assetSubJobService, times(1)).sendAssetSubJobMessageToKafka(any(AssetSubJobEvent.class));
  }

  @Test
  public void testMetadata() throws InterruptedException {
    final UUID subJobId = UUID.randomUUID();
    String minioMetadataFileURL = null;
    String metadataFileName = "some-metadata-file.json";
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    var subJobDto = DataMoverTestUtil.createAssetSubJobDto();
    subJobDto.setId(null);
    subJobDto.setMetaDataFileUrl(metadataFileName);
    subJobDto.setFilePath(List.of("any folder"));
    subJobDto.setType(AssetCategory.METADATA);

    when(assetSubJobService.create(any(AssetSubJobDto.class))).thenReturn(subJobId.toString());

    var assetTask =
        new AssetTask(
            assetSubJobService,
            subJobDto,
            metadataFileName,
            minioMetadataFileURL,
            null,
            userRoles,
            null);
    assetTask.run();

    verify(assetSubJobService, times(1)).create(any(AssetSubJobDto.class));
    var event = ArgumentCaptor.forClass(AssetSubJobEvent.class);
    verify(assetSubJobService, times(1)).sendAssetSubJobMessageToKafka(event.capture());
    assertEquals(
        com.xperi.schema.subjobevent.AssetCategory.METADATA, event.getValue().getSubJobType());
  }

  private AssetMetadata createNewAssetMetadata() {
    var file = new File();
    file.setMetaFileName("labels1.json");
    file.setFileName(List.of("File1.png"));
    file.setOwner("");

    var assetMetadata = new AssetMetadata();
    assetMetadata.setFilePartMetadata(file);
    assetMetadata.setMetaFileName("labels_new_format.json");
    assetMetadata.setParameters("");
    assetMetadata.setUiPartMetadata(Collections.emptyMap());

    return assetMetadata;
  }
}
