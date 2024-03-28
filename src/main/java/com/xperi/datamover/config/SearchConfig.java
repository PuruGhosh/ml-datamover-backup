/** */
package com.xperi.datamover.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;
import java.time.Duration;

/** Elastic search config */
@Data
@Configuration
@ConfigurationProperties(prefix = "search")
public class SearchConfig {

  @NotBlank private String assetIndexUrl;
  @NotBlank private String assetSearchUrlByName;
  @NotBlank private String assetSearchUrlByKeyword;
  private Duration connectTimeout = Duration.ofSeconds(10);

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.setConnectTimeout(connectTimeout).build();
  }
}
