package com.xperi.datamover.service;

import com.xperi.datamover.config.AssetConfigProperties;
import com.xperi.datamover.dto.AssetDownloadDto;
import com.xperi.datamover.dto.RestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;

/** This class contains different methods which call the ml-asset micro-service APIs */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceClient {
  private final RestTemplate restTemplate;
  private final AssetConfigProperties assetConfigProperties;

  /**
   * This method gets the content of a file stored in minio
   *
   * @param minioFileUrl Minio file url
   * @return Content of a file in the form of byte array
   */
  public byte[] getContent(String minioFileUrl) {
    try {
      log.debug("Minio file url: {}", minioFileUrl);

      String url = assetConfigProperties.getContentUrl();
      var typeRef = new ParameterizedTypeReference<RestResponse<AssetDownloadDto>>() {};
      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      String formatted_URL = MessageFormat.format(url, minioFileUrl);
      ResponseEntity<RestResponse<AssetDownloadDto>> responseEntity =
          restTemplate.exchange(formatted_URL, HttpMethod.GET, entity, typeRef);

      RestResponse<AssetDownloadDto> response = responseEntity.getBody();
      return response.getData().getContent();
    } catch (Exception e) {
      log.error("Unable to get content. Details: {}", e.getLocalizedMessage(), e);
      return null;
    }
  }
}
