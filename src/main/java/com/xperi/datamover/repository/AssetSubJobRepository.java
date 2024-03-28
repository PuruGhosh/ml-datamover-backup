package com.xperi.datamover.repository;

import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.dto.CountBasedOnStatusDto;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.util.AssetJobMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/** Repository for sub-job related operation */
@Repository
public interface AssetSubJobRepository extends MongoRepository<AssetSubJobEntity, UUID> {

  /**
   * To retrieve count of sub-jobs for a particular status
   *
   * @param parentJobId Parent job id for the sub-job
   * @param status Status of an asset sub job
   * @return Count of sub-jobs
   */
  Long countByParentJobIdAndStatus(UUID parentJobId, AssetJobStatus status);

  /**
   * To retrieve sub job details using parent job id
   *
   * @param id Parent job id for the sub-jobs
   * @return List of asset sub job details
   */
  List<AssetSubJobEntity> findByParentJobId(UUID id);

  /**
   * To retrieve pageable sub-job details using parent job id
   *
   * @param id
   * @param pageable
   * @return
   */
  Page<AssetSubJobEntity> findByParentJobId(UUID id, Pageable pageable);

  /**
   * To retrieve pageable sub-job details using parent job id and status
   *
   * @param id
   * @param status
   * @param pageable
   * @return
   */
  Page<AssetSubJobEntity> findByParentJobIdAndStatus(UUID id,AssetJobStatus status, Pageable pageable);

  /**
   * To retrieve sub job details by status and Asset category
   *
   * @param status Status of an asset sub job
   * @param type Asset category
   * @return Sublist of list of asset sub job details
   */
  Page<AssetSubJobEntity> findByStatusAndType(
          AssetJobStatus status, AssetCategory type, Pageable pageable);

  /**
   * To retrieve count of sub-jobs for each sub job status
   *
   * @param id Parent job id for the sub-jobs
   * @param mongoTemplate Mongo template
   * @return the status of sub jobs with their counts
   */
  default Map<AssetJobStatus, Long> countBasedOnStatus(String id, MongoTemplate mongoTemplate) {
    GroupOperation groupOperation = Aggregation.group("status").count().as("total");
    MatchOperation matchStage =
        Aggregation.match(new Criteria("parentJobId").is(AssetJobMapper.stringToUuid(id)));
    Aggregation aggregation = Aggregation.newAggregation(matchStage, groupOperation);
    AggregationResults<CountBasedOnStatusDto> result =
        mongoTemplate.aggregate(aggregation, AssetSubJobEntity.class, CountBasedOnStatusDto.class);
    return result.getMappedResults().stream()
        .collect(
            Collectors.toMap(
                status -> AssetJobStatus.valueOf(status.getId()), status -> status.getTotal()));
  }

  /**
   * To find an asset details with latest semantic version id
   *
   * @param fileName Name of the file
   * @param status Status-COMPLETE
   * @return An optional AssetSubJobEntity
   */
  Optional<AssetSubJobEntity> findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
      String fileName, String status);

  /**
   * To retrieve count of sub-jobs for a given parentJobId
   * @param parentJobId Parent job id for the sub-job
   * @return Count of sub-jobs
   */
  Long countByParentJobId(UUID parentJobId);
}




