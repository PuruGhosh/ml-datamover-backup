package com.xperi.datamover.controller;

import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.dto.MetadataJobRequestDto;
import com.xperi.datamover.dto.RestResponse;
import com.xperi.datamover.dto.RetrieveAssetJobDto;
import com.xperi.datamover.service.AssetJobService;
import com.xperi.datamover.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** This controller contains all the job related APIs */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/jobs")
@Slf4j
@RequiredArgsConstructor
public class AssetJobController {

  private final AssetJobService assetJobService;

  /**
   * This API will create a Job document in the MongoDB
   *
   * @param assetJobDto to takes necessary request parameters to create a job
   * @return It returns the same Job document after creating a Job in database
   */
  @PostMapping
  public ResponseEntity<RestResponse<AssetJobDto>> create(
      @Valid @RequestBody AssetJobDto assetJobDto,
      @RequestHeader("jwt-xperi-claim") String encodedJwtTokenPayload)
      throws Exception {
    // UserRoles are being retrieved from JwtToken in order to add in Kafka message since userRoles
    // are required in ML-Index.
    var userRoles = TokenUtil.getUserRoles(encodedJwtTokenPayload);

    log.info("Request details for creating job  :  {} with user roles {}", assetJobDto, userRoles);
    final RestResponse<AssetJobDto> response = new RestResponse<>();
    final AssetJobDto result = assetJobService.create(assetJobDto, userRoles);
    log.info("Successfully Created job with Id {} - {} ", result.getId(), result);
    response.setData(result);
    return ResponseEntity.ok(response);
  }

  /**
   * This API will retrieve a Job document in the MongoDB
   *
   * @return It returns RetrieveAssetJobDto
   */
  @GetMapping
  public ResponseEntity<RestResponse<Page<RetrieveAssetJobDto>>> retrieve(Pageable p) {
    final RestResponse<Page<RetrieveAssetJobDto>> response = new RestResponse<>();
    final Page<RetrieveAssetJobDto> result = assetJobService.retrieve(p);
    log.debug("Successfully retrieved job - {}", result);
    response.setData(result);
    return ResponseEntity.ok(response);
  }
  /**
   * This API will retrieve a Job document with given on job Id
   *
   * @return It returns RetrieveAssetJobDto
   */
  @GetMapping("/{id}")
  public ResponseEntity<RestResponse<RetrieveAssetJobDto>> retrieveAssetJobById(
      @PathVariable("id") UUID id) {
    final RestResponse<RetrieveAssetJobDto> response = new RestResponse<>();
    final RetrieveAssetJobDto result = assetJobService.retrieveAssetJobById(id);
    log.debug("Successfully retrieved job - {}", result);
    response.setData(result);
    return ResponseEntity.ok(response);
  }

  /**
   * This API will create a Job document in the MongoDB with jobType Metadata
   *
   * @param metadataJobDto to takes necessary request parameters to create a job
   * @return It returns the same Job document after creating a Job in database
   */
  @PostMapping("/metadata")
  public ResponseEntity<RestResponse<AssetJobDto>> createMetadataJob(
      @Valid @RequestBody MetadataJobRequestDto metadataJobDto) throws Exception {
    log.info("request details for creating job  :  {}", metadataJobDto);
    final AssetJobDto result = assetJobService.createMetadataJob(metadataJobDto);
    log.info("Successfully created job - {}", result);
    final RestResponse<AssetJobDto> response = new RestResponse<>();
    response.setData(result);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/existsByJobName/{jobName}")
  public ResponseEntity<RestResponse<Map<String, Boolean>>> existsByJobName(
      @PathVariable String jobName) {
    Map<String, Boolean> responseObject = new HashMap<>();
    var exists = assetJobService.existsByJobName(jobName);
    responseObject.put("exists", exists);
    RestResponse<Map<String, Boolean>> response = new RestResponse<>();
    response.setData(responseObject);
    var status = exists ? HttpStatus.OK : HttpStatus.NOT_FOUND;
    return new ResponseEntity<>(response, status);
  }
}
