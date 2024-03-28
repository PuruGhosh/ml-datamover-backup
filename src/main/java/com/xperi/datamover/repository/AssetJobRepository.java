package com.xperi.datamover.repository;

import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.entity.AssetJobEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Repository for job related operation */
@Repository
public interface AssetJobRepository extends MongoRepository<AssetJobEntity, UUID> {

  /**
   * To retrieve asset details between two dates
   *
   * @param startDate
   * @param endDate
   * @param status
   * @return All Jobs with status and between time
   */
  List<AssetJobEntity> findByCreatedAtBetweenAndStatusIn(
          Date startDate, Date endDate, List<AssetJobStatus> status);

  @Query(value = "{ 'jobName' : ?0}", exists = true)
  boolean existsByJobName(String jobName);
}
