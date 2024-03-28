package com.xperi.datamover.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xperi.datamover.dto.*;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.service.SearchServiceClient;
import com.xperi.datamover.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** This controller contains all the sub job related APIs */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/sub-jobs")
@Slf4j
@RequiredArgsConstructor
public class AssetSubJobController {

  private final AssetSubJobService assetSubJobService;
  private final SearchServiceClient searchServiceClient;

  /**
   * Find all existing sub jobs by parent job id
   *
   * @return list of collection
   */
  @GetMapping("{jobId}")
  public ResponseEntity<RestResponse<Page<AssetSubJobDto>>> findByParentJobId(
      @PathVariable String jobId, Pageable p) {
    log.debug("find all existing child jobs for jobId:{}", jobId);
    final RestResponse<Page<AssetSubJobDto>> response = new RestResponse<>();
    response.setData(assetSubJobService.findByParentJobIdPageable(jobId, p));
    return ResponseEntity.ok(response);
  }

  /**
   * Find all existing sub-jobs by parent job Id and job status
   *
   * @param jobId parent jobId
   * @param status
   * @return list of sub-jobs
   */
  @GetMapping("{jobId}/{status}")
  public ResponseEntity<RestResponse<Page<AssetSubJobDto>>> findByParentJobIdAndStatus(
          @PathVariable String jobId,@PathVariable String status, Pageable p) {
    log.debug("find all existing child jobs for jobId:{}", jobId);
    final RestResponse<Page<AssetSubJobDto>> response = new RestResponse<>();
    response.setData(assetSubJobService.findByParentJobIdAndStatusPageable(jobId,status, p));
    return ResponseEntity.ok(response);
  }


  /**
   * Find all existing sub jobs in Completed State
   *
   * @param dto AssetSubJobRequestDto
   * @return Sublist of list of asset sub job details
   */
  @PostMapping("/retrieve")
  public ResponseEntity<RestResponse<AssetSubJobResponseDto>> findAssetSubJobs(
      @RequestBody AssetSubJobRequestDto dto) {
    log.debug("find all asset sub jobs with given status");
    final RestResponse<AssetSubJobResponseDto> response = new RestResponse<>();
    response.setData(assetSubJobService.findAssetSubJobs(dto));
    return ResponseEntity.ok(response);
  }

  /**
   * @param searchText
   * @return
   */
  @GetMapping("/search/{searchText}")
  public ResponseEntity<RestResponse<List<AssetSubJobDto>>> searchAsset(
      @PathVariable String searchText) {
    final RestResponse<List<AssetSubJobDto>> response = new RestResponse<>();
    final List<String> searchAssetIds = searchServiceClient.searchAsset(searchText);
    final List<AssetSubJobDto> list;
    if (CollectionUtils.isEmpty(searchAssetIds)) {
      list = List.of();
    } else {
      list = assetSubJobService.findAllByIds(searchAssetIds);
    }
    response.setData(list);
    return ResponseEntity.ok(response);
  }

  /**
   * To update bulk metadata
   *
   * @param updateBulkMetadataRequestDto
   * @return AssetJobDto
   */
  @PutMapping("/updateBulkMetadata")
  public ResponseEntity<RestResponse<RetrieveAssetJobDto>> updateBulkMetadata(
      @RequestBody UpdateBulkMetadataRequestDto updateBulkMetadataRequestDto,
      @RequestHeader("jwt-xperi-claim") String encodedJwtTokenPayload)
      throws JsonProcessingException {
    // UserRoles are being retrieved from JwtToken in order to add in Kafka message since userRoles
    // are required in ML-Index.
    var userRoles = TokenUtil.getUserRoles(encodedJwtTokenPayload);
    log.info(
        "request details for updating bulk metadata :  {} with user roles {}",
        updateBulkMetadataRequestDto,
        userRoles);
    final RestResponse<RetrieveAssetJobDto> response = new RestResponse<>();
    response.setData(
        assetSubJobService.updateBulkMetadata(updateBulkMetadataRequestDto, userRoles));
    return ResponseEntity.ok(response);
  }
}
