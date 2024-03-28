package com.xperi.datamover.service;

import com.xperi.datamover.config.SearchConfig;
import com.xperi.datamover.dto.SearchAssetDto;
import com.xperi.datamover.util.AssetSubJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * This class contains different methods which call the ml-index & ml-search micro-services APIs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceClient {

  private final SearchConfig searchConfig;
  private final AssetSubJobMapper assetSubJobMapper;
  @Autowired RestTemplate restTemplate;

  /**
   * This method index the asset data
   *
   * @param searchAssetDto Asset details
   * @return True or False based on indexing status
   */
  public boolean indexAsset(SearchAssetDto searchAssetDto) {
    try {
      final HttpEntity<SearchAssetDto> request = new HttpEntity<>(searchAssetDto);
      final String assetId =
          restTemplate.postForObject(searchConfig.getAssetIndexUrl(), request, String.class);
      log.info("Asset id indexed {}", assetId);
      return true;
    } catch (Exception e) {
      log.error("exception while indexing {}", searchAssetDto);
      return false;
    }
  }

  /**
   * call the search service
   *
   * @param searchTerm
   * @return
   */
  public List<String> searchAsset(String searchTerm) {
    List<String> assetIds = List.of();
    ResponseEntity<List<String>> responseEntity;
    // search the collection
    try {
      responseEntity =
          restTemplate.exchange(
              searchConfig.getAssetSearchUrlByName() + "/" + searchTerm,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {});
      assetIds = responseEntity.getBody();
      if (CollectionUtils.isEmpty(assetIds)) {
        responseEntity =
            restTemplate.exchange(
                searchConfig.getAssetSearchUrlByKeyword() + "/" + searchTerm,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});
        assetIds = responseEntity.getBody();
      }
    } catch (Exception e) {
      log.error("exception while searching {} {}", searchTerm, e);
    }
    return assetIds;
  }
}
