package com.xperi.datamover.service;

import com.xperi.datamover.constants.AssetCategory;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

/** This class contains all the job related operations */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetJobService {

  private static final long TOTAL = -1L;
  private static final long PROGRESS_OK = 0L;
  private static final long PROGRESS_FAILED = 0L;
  private final AssetJobRepository assetJobRepository;
  private final AssetJobMapper assetJobMapper;
  private final AssetSubJobService assetSubJobService;

  /**
   * This method creates a job & saves in database
   *
   * @param assetJobDto
   * @return
   */
  public AssetJobDto create(AssetJobDto assetJobDto, List<String> userRoles) throws Exception {
    Assert.notNull(assetJobDto, "Job details are empty");

    if (assetJobDto.getAssetLocationType().equals(AssetLocationType.FOLDER)
        && assetJobDto.getLocations().size() > 1) {
      throw new DataMoverException("Only one folder is allowed to upload at a time");
    }

    // To set the default values in the sub-jobs status accumulator
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, TOTAL);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, PROGRESS_OK);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, PROGRESS_FAILED);

    final AssetJobEntity assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);
    assetJobEntity.setId(UUID.randomUUID());
    assetJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobEntity.setSubJobStatusAccumulator(subJobStatusAccumulator);
    assetJobEntity.setFolder(assetJobDto.getLocations().get(0));
    assetJobEntity.setDescription(assetJobDto.getDescription().strip());

    // To set the value in the initial metadata file
    if (StringUtils.isNotEmpty(assetJobDto.getMetaDataFileName())) {
      assetJobEntity.setInitialMetadataFile(true);
    }

    log.debug("Job details before creating entry in database : {}", assetJobEntity);
    var entity = assetJobRepository.save(assetJobEntity);
    log.info("Job details saved in database : {}", assetJobEntity);

    AssetJobDto createdJob = assetJobMapper.fromJobEntity(entity);
    assetSubJobService.processSubJobs(createdJob, userRoles);

    return createdJob;
  }

  /**
   * To retrieve Pageable job details
   *
   * @return
   */
  public Page<RetrieveAssetJobDto> retrieve(Pageable p) {
    Page<AssetJobEntity> jobList = assetJobRepository.findAll(p);

    Page<RetrieveAssetJobDto> retrieveJobDtoPage =
        (Page<RetrieveAssetJobDto>)
            jobList.map(
                new Function<AssetJobEntity, RetrieveAssetJobDto>() {
                  @Override
                  public RetrieveAssetJobDto apply(AssetJobEntity entity) {
                    // AssetJobEntity to RetrieveAssetJobDto mapping
                    return assetJobMapper.retrieveAssetJobDtoFromJobEntity(entity);
                  }
                });

    // return assetSubJobService.subJobCountByParentJobIdAndStatusPageable(retrieveJobDtoPage);
    return retrieveJobDtoPage;
  }

  /**
   * To retrieve job details with given job Id
   *
   * @return
   */
  public RetrieveAssetJobDto retrieveAssetJobById(UUID id) {
    Assert.notNull(id, "Asset Job id is null");
    final AssetJobEntity assetJobEntity = assetJobRepository.findById(id).get();
    return assetJobMapper.retrieveAssetJobDtoFromJobEntity(assetJobEntity);
  }

  /**
   * To retrieve all jobs with status between specified time
   *
   * @param searchStatusList List of status
   * @param hours Hours for retrieval jobs
   * @return All jobs with status and between time
   */
  public List<RetrieveAssetJobDto> retrieveAllWithStatusAndTime(
      List<AssetJobStatus> searchStatusList, int hours) {

    Calendar calendar = Calendar.getInstance();
    Date currentTime = calendar.getTime();
    calendar.add(Calendar.HOUR, -(hours));
    Date startTime = calendar.getTime();
    List<AssetJobEntity> jobList =
        assetJobRepository.findByCreatedAtBetweenAndStatusIn(
            startTime, currentTime, searchStatusList);
    return assetJobMapper.toRetrieveAssetJobDtoList(jobList);
  }

  /**
   * To update the status and history in the job
   *
   * @param id
   * @param status
   * @return
   */
  public AssetJobDto updateStatus(String id, AssetJobStatus status) {
    log.debug("Update status of job : {} with {}", id, status);
    Optional<AssetJobEntity> foundJob =
        assetJobRepository.findById(AssetJobMapper.stringToUuid(id));
    if (foundJob.isPresent()) {
      var jobEntity = foundJob.get();
      if (!jobEntity.getStatus().equals(status)) {
        jobEntity.setStatus(status);
        log.debug("Job details before updating {}", jobEntity);
        jobEntity = assetJobRepository.save(jobEntity);
      }
      return assetJobMapper.fromJobEntity(jobEntity);
    } else {
      log.debug("Job details does not exist, provided id is {} ", id);
      throw new DataMoverException("Job details does not exist. id=" + id);
    }
  }

  /**
   * This method creates a job & saves in database with jobType Metadata
   *
   * @param metadataJobRequestDto
   * @return
   */
  public AssetJobDto createMetadataJob(MetadataJobRequestDto metadataJobRequestDto)
      throws Exception {

    var timestamp = Calendar.getInstance().getTime();
    // To set the default values in the sub-jobs status accumulator
    var subJobStatusAccumulator =
        Map.of(
            AssetJobStatusAccumulator.TOTAL, metadataJobRequestDto.getNoOfMetadata(),
            AssetJobStatusAccumulator.PROGRESS_OK, PROGRESS_OK,
            AssetJobStatusAccumulator.PROGRESS_FAILED, PROGRESS_FAILED);

    final AssetJobEntity assetJobEntity = assetJobMapper.toJobEntity(metadataJobRequestDto);
    assetJobEntity.setId(UUID.randomUUID());
    assetJobEntity.setCreatedAt(timestamp);
    assetJobEntity.setUpdatedAt(timestamp);
    assetJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobEntity.setSubJobStatusAccumulator(subJobStatusAccumulator);
    assetJobEntity.setJobType(AssetCategory.METADATA);
    assetJobEntity.setDescription(metadataJobRequestDto.getDescription().strip());
    log.info("Metadata Job details before creating entry in database : {}", assetJobEntity);
    var entity = assetJobRepository.save(assetJobEntity);
    return assetJobMapper.fromJobEntity(entity);
  }

  public boolean existsByJobName(String jobName) {
    Assert.hasText(jobName, "Job name is not valid.");
    boolean exists = assetJobRepository.existsByJobName(jobName);
    log.info("Job Name: {}, Exists: {}.",jobName, exists);
    return  (exists);
  }
}
