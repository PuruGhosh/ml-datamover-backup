package com.xperi.datamover.controller;

import com.xperi.datamover.dto.AssetDataLoaderDto;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.dto.RestResponse;
import com.xperi.datamover.service.AssetSubJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/** This controller contains all the data loader related APIs */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/data-loader")
@Slf4j
@RequiredArgsConstructor
public class AssetDataLoaderController {

  private final AssetSubJobService assetSubJobService;

  /**
   * Finds asset sub-job details with asset ids
   *
   * @param dto AssetDataLoaderDto
   * @return ResponseEntity with list of assets sub-job details
   */
  @PostMapping("/assets")
  public ResponseEntity<RestResponse<List<AssetSubJobDto>>> findAssetsByIds(
      @Valid @RequestBody AssetDataLoaderDto dto) {
    final RestResponse<List<AssetSubJobDto>> response = new RestResponse<>();
    final List<AssetSubJobDto> result = assetSubJobService.findAllByIds(dto.getAssetIds());
    response.setData(result);
    return ResponseEntity.ok(response);
  }
}
