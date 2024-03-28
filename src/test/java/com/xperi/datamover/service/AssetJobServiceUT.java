package com.xperi.datamover.service;

import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.dto.MetadataJobRequestDto;
import com.xperi.datamover.dto.RetrieveAssetJobDto;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.exception.DataMoverException;
import com.xperi.datamover.repository.AssetJobRepository;
import com.xperi.datamover.util.AssetJobMapper;
import com.xperi.datamover.util.DataMoverTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** This class is used for unit testing Job Service */
@ExtendWith(MockitoExtension.class)
public class AssetJobServiceUT {

  private final AssetJobMapper assetJobMapper = Mappers.getMapper(AssetJobMapper.class);
  @Mock private AssetJobRepository assetJobRepository;
  @Mock private AssetSubJobService assetSubJobService;
  @InjectMocks private AssetJobService assetJobService;

  @BeforeEach
  void createTestClass() {
    assetJobService = new AssetJobService(assetJobRepository, assetJobMapper, assetSubJobService);
  }

  @Test
  @DisplayName("Testing create job method")
  public void testCreateJob() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final AssetJobEntity assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    when(assetJobRepository.save(any(AssetJobEntity.class))).thenReturn(assetJobEntity);

    final AssetJobDto result = assetJobService.create(assetJobDto, userRoles);
    verify(assetJobRepository, times(1)).save(any(AssetJobEntity.class));

    assertEquals(assetJobDto.getOwnerId(), result.getOwnerId(), "Owner id is not equal");
    assertEquals(assetJobDto.getMessage(), result.getMessage(), "Message is not equal");
    assertEquals(assetJobDto.getJobName(), result.getJobName(), "Job name is not equal");
    assertEquals(assetJobDto.getDescription(), result.getDescription(), "Description is not equal");
    assertEquals(assetJobDto.getStatus(), result.getStatus(), "Status is not equal");
    assertEquals(
        assetJobDto.getAssetLocationType(),
        result.getAssetLocationType(),
        "Location type is not equal");
    assertEquals(assetJobDto.getLocations(), result.getLocations(), "Locations are not equal");
    assertEquals(assetJobDto.getMetaData(), result.getMetaData(), "Meta data are not equal");
    assertEquals(assetJobDto.getMetaData(), result.getMetaData(), "Meta data are not equal");
    assertEquals(
        assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
        result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
        "Total sub job counts are not equal");
    assertEquals(
        assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_OK),
        result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_OK),
        "PROGRESS_OK sub job counts are not equal");
    assertEquals(
        assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_FAILED),
        result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_FAILED),
        "PROGRESS_OK sub job counts are not equal");
  }

  // this would be removed
  @Test
  @DisplayName(
      "Testing create job method when location type is folder & locations contain multiple folder")
  public void testCreateJob_whenLocationIsFolder() {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();

    List<String> fileNames = new ArrayList<>();
    fileNames.add("folder1");
    fileNames.add("folder2");
    assetJobDto.setAssetLocationType(AssetLocationType.FOLDER);
    assetJobDto.setLocations(fileNames);

    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    assertThrows(
        DataMoverException.class,
        () -> {
          assetJobService.create(assetJobDto, userRoles);
        });
  }

  @Test
  @DisplayName("Testing create job method when job details are empty")
  public void testCreateJob_whenRequestDtoIsBlank() {
    AssetJobDto assetJobDto = null;
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          assetJobService.create(assetJobDto, userRoles);
        });
  }

  /** Test method for retrieving all asset jobs */
  @Test
  @DisplayName("Test Retrieve all asset jobs OK")
  public void testRetrieveAll() {

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, 5L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 4L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 1L);

    // test asset job entity
    final AssetJobEntity jobEntity = new AssetJobEntity();
    jobEntity.setId(UUID.randomUUID());
    jobEntity.setOwnerId("testOwnerId");
    jobEntity.setStatus(AssetJobStatus.ERROR);
    jobEntity.setCreatedAt(Calendar.getInstance().getTime());
    jobEntity.setJobName("testJobName");
    jobEntity.setSubJobStatusAccumulator(subJobStatusAccumulator);

    // list of test asset jon entity
    List<AssetJobEntity> testJobEntityList = new ArrayList<>();
    testJobEntityList.add(jobEntity);

    Pageable p = PageRequest.of(5, 20);
    Page<AssetJobEntity> assetJobEntityPage = new PageImpl<>(testJobEntityList);

    when(assetJobRepository.findAll(p)).thenReturn(assetJobEntityPage);

    Page<RetrieveAssetJobDto> result = assetJobService.retrieve(p);

    verify(assetJobRepository, times(1)).findAll(p);
    assertEquals(
        jobEntity.getOwnerId(), result.toList().get(0).getOwnerId(), "OwnerId is not equal");
    assertEquals(jobEntity.getStatus(), result.toList().get(0).getStatus(), "status is not equal");
    assertEquals(jobEntity.getJobName(), result.toList().get(0).getJobName(), "Job Name is equal");
    assertEquals(
        5L,
        result.toList().get(0).getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
        "TOTAL sub jobs count is not equal");
    assertEquals(
        4L,
        result
            .toList()
            .get(0)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_OK),
        "PROGRESS_OK sub jobs count is not equal");
    assertEquals(
        1L,
        result
            .toList()
            .get(0)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_FAILED),
        "PROGRESS_FAILED sub jobs count is not equal");
  }

  /** Test method for retrieving count of multiple sub asset jobs with same status */
  @Test
  @DisplayName("Test Retrieve count of all sub asset jobs with same status")
  public void testRetrieveTestAll() {

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulatorOne = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulatorOne.put(AssetJobStatusAccumulator.TOTAL, 5L);
    subJobStatusAccumulatorOne.put(AssetJobStatusAccumulator.PROGRESS_OK, 5L);
    subJobStatusAccumulatorOne.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);

    // test asset job entity
    final AssetJobEntity jobEntityOne = new AssetJobEntity();
    jobEntityOne.setId(UUID.randomUUID());
    jobEntityOne.setOwnerId("testOwnerId");
    jobEntityOne.setStatus(AssetJobStatus.COMPLETE);
    jobEntityOne.setCreatedAt(Calendar.getInstance().getTime());
    jobEntityOne.setJobName("testJobName");
    jobEntityOne.setSubJobStatusAccumulator(subJobStatusAccumulatorOne);

    // list of asset job entity
    List<AssetJobEntity> testJobEntityList = new ArrayList<>();
    testJobEntityList.add(jobEntityOne);

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulatorTwo = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulatorTwo.put(AssetJobStatusAccumulator.TOTAL, 3L);
    subJobStatusAccumulatorTwo.put(AssetJobStatusAccumulator.PROGRESS_OK, 3L);
    subJobStatusAccumulatorTwo.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);

    final AssetJobEntity jobEntityTwo = new AssetJobEntity();
    jobEntityTwo.setId(UUID.randomUUID());
    jobEntityTwo.setOwnerId("testOwnerId2");
    jobEntityTwo.setStatus(AssetJobStatus.COMPLETE);
    jobEntityTwo.setCreatedAt(Calendar.getInstance().getTime());
    jobEntityTwo.setJobName("testJobName2");
    jobEntityTwo.setSubJobStatusAccumulator(subJobStatusAccumulatorTwo);

    testJobEntityList.add(jobEntityTwo);

    // test retrieve asset job dto - 1
    final RetrieveAssetJobDto retrieveAssetJobDtoTestOne = new RetrieveAssetJobDto();
    retrieveAssetJobDtoTestOne.setId(UUID.randomUUID().toString());
    retrieveAssetJobDtoTestOne.setOwnerId("testOwnerId");
    retrieveAssetJobDtoTestOne.setStatus(AssetJobStatus.COMPLETE);
    retrieveAssetJobDtoTestOne.setCreatedAt(Calendar.getInstance().getTime());
    retrieveAssetJobDtoTestOne.setJobName("testJobName");

    // test status count details - 1
    Map<AssetJobStatus, Long> subJobStatusTestOne = new HashMap<AssetJobStatus, Long>();
    subJobStatusTestOne.put(AssetJobStatus.COMPLETE, (long) 2);
    subJobStatusTestOne.put(AssetJobStatus.ERROR, (long) 1);
    subJobStatusTestOne.put(AssetJobStatus.IN_PROGRESS, (long) 3);
    retrieveAssetJobDtoTestOne.setSubJobStatus(subJobStatusTestOne);

    // list of test retrieve asset job dto
    List<RetrieveAssetJobDto> retrieveAssetJobDtos = new ArrayList<>();
    retrieveAssetJobDtos.add(retrieveAssetJobDtoTestOne);

    // test retrieve asset job dto - 2
    final RetrieveAssetJobDto retrieveAssetJobDtoTestTwo = new RetrieveAssetJobDto();
    retrieveAssetJobDtoTestTwo.setId(UUID.randomUUID().toString());
    retrieveAssetJobDtoTestTwo.setOwnerId("testOwnerId2");
    retrieveAssetJobDtoTestTwo.setStatus(AssetJobStatus.COMPLETE);
    retrieveAssetJobDtoTestTwo.setCreatedAt(Calendar.getInstance().getTime());
    retrieveAssetJobDtoTestTwo.setJobName("testJobName2");

    // test status count details - 2
    Map<AssetJobStatus, Long> subJobStatusTestTwo = new HashMap<AssetJobStatus, Long>();
    subJobStatusTestTwo.put(AssetJobStatus.COMPLETE, (long) 1);
    subJobStatusTestTwo.put(AssetJobStatus.ERROR, (long) 0);
    subJobStatusTestTwo.put(AssetJobStatus.IN_PROGRESS, (long) 2);
    retrieveAssetJobDtoTestTwo.setSubJobStatus(subJobStatusTestTwo);

    retrieveAssetJobDtos.add(retrieveAssetJobDtoTestTwo);

    Pageable p = PageRequest.of(5, 20);
    Page<AssetJobEntity> assetJobEntityPage = new PageImpl<>(testJobEntityList);

    when(assetJobRepository.findAll(p)).thenReturn(assetJobEntityPage);

    Page<RetrieveAssetJobDto> result = assetJobService.retrieve(p);

    verify(assetJobRepository, times(1)).findAll(p);
    assertEquals(
        retrieveAssetJobDtoTestOne.getOwnerId(),
        result.toList().get(0).getOwnerId(),
        "OwnerId is not equal");
    assertEquals(
        retrieveAssetJobDtoTestOne.getStatus(),
        result.toList().get(0).getStatus(),
        "status is not equal");
    assertEquals(
        retrieveAssetJobDtoTestOne.getJobName(),
        result.toList().get(0).getJobName(),
        "Job Name is not equal");
    assertEquals(
        retrieveAssetJobDtoTestTwo.getOwnerId(),
        result.toList().get(1).getOwnerId(),
        "OwnerId is not equal");
    assertEquals(
        retrieveAssetJobDtoTestTwo.getStatus(),
        result.toList().get(1).getStatus(),
        "status is not equal");
    assertEquals(
        retrieveAssetJobDtoTestTwo.getJobName(),
        result.toList().get(1).getJobName(),
        "Job Name is not equal");
    assertEquals(
        5L,
        result.toList().get(0).getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL));
    assertEquals(
        5L,
        result
            .toList()
            .get(0)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_OK));
    assertEquals(
        0L,
        result
            .toList()
            .get(0)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_FAILED));
    assertEquals(
        3L,
        result.toList().get(1).getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL));
    assertEquals(
        3L,
        result
            .toList()
            .get(1)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_OK));
    assertEquals(
        0L,
        result
            .toList()
            .get(1)
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.PROGRESS_FAILED));
  }

  /** Test method for retrieving asset job with given id */
  @Test
  @DisplayName("Testing retrieve Asset job by id API")
  public void testRetrieveAssetJobById() {

    // To set the values in the sub-jobs status accumulator
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, 5L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 4L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 1L);

    // test asset job entity
    final AssetJobEntity jobEntity = new AssetJobEntity();
    jobEntity.setId(UUID.randomUUID());
    jobEntity.setId(UUID.randomUUID());
    jobEntity.setOwnerId("testOwnerId");
    jobEntity.setStatus(AssetJobStatus.ERROR);
    jobEntity.setCreatedAt(Calendar.getInstance().getTime());
    jobEntity.setJobName("testJobName");
    jobEntity.setSubJobStatusAccumulator(subJobStatusAccumulator);

    when(assetJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(jobEntity));

    var result = assetJobService.retrieveAssetJobById(jobEntity.getId());

    verify(assetJobRepository, times(1)).findById(jobEntity.getId());
    assertEquals(
            jobEntity.getOwnerId(), result.getOwnerId(), "OwnerId is not equal");
    assertEquals(jobEntity.getStatus(), result.getStatus(), "status is not equal");
    assertEquals(jobEntity.getJobName(), result.getJobName(), "Job Name is equal");
    assertEquals(
            5L,
            result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
            "TOTAL sub jobs count is not equal");
    assertEquals(
            4L,
            result
                    .getSubJobStatusAccumulator()
                    .get(AssetJobStatusAccumulator.PROGRESS_OK),
            "PROGRESS_OK sub jobs count is not equal");
    assertEquals(
            1L,
            result
                    .getSubJobStatusAccumulator()
                    .get(AssetJobStatusAccumulator.PROGRESS_FAILED),
            "PROGRESS_FAILED sub jobs count is not equal");
  }

  @Test
  @DisplayName("Testing create Metadata job method")
  public void testCreateMetadataJob() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final MetadataJobRequestDto metadataJobRequestDto = DataMoverTestUtil.createMetadataJobRequestDto();
    metadataJobRequestDto.setNoOfMetadata(assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL));
    final AssetJobEntity assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);
    when(assetJobRepository.save(any(AssetJobEntity.class))).thenReturn(assetJobEntity);

    final AssetJobDto result = assetJobService.createMetadataJob(metadataJobRequestDto);
    verify(assetJobRepository, times(1)).save(any(AssetJobEntity.class));

    assertEquals(assetJobDto.getOwnerId(), result.getOwnerId(), "Owner id is not equal");
    assertEquals(assetJobDto.getMessage(), result.getMessage(), "Message is not equal");
    assertEquals(assetJobDto.getJobName(), result.getJobName(), "Job name is not equal");
    assertEquals(assetJobDto.getDescription(), result.getDescription(), "Description is not equal");
    assertEquals(assetJobDto.getStatus(), result.getStatus(), "Status is not equal");
    assertEquals(assetJobDto.getMetaData(), result.getMetaData(), "Meta data are not equal");
    assertEquals(
            assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
            result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL),
            "Total sub job counts are not equal");
    assertEquals(
            assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_OK),
            result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_OK),
            "PROGRESS_OK sub job counts are not equal");
    assertEquals(
            assetJobDto.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_FAILED),
            result.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_FAILED),
            "PROGRESS_OK sub job counts are not equal");
  }

  @Test
  @DisplayName("Testing existsByobName")
  void testExistsByJobName() {
    final String jobName = "testJobName";
    when(assetJobRepository.existsByJobName(any(String.class))).thenReturn(true);
    assertEquals(true,assetJobService.existsByJobName(jobName));
  }
}
