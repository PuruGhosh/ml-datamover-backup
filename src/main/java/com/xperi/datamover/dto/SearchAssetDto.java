package com.xperi.datamover.dto;

import lombok.Data;

import java.util.Map;

/** This is a DTO class for communicating with ml-index and ml-search micro-services */
@Data
public class SearchAssetDto {

  private String id;

  private String name;

  private String jobName;

  private Map<String, String> metadata;
}
