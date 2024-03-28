package com.xperi.datamover.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssetSubJobResponseDto {

  // List of assets
  List<AssetSubJobDto> assetList;

  // Total number of pages
  private int totalPages;

  // Current page number
  private int currentPage;

  // Total number of associations
  private long totalItems;
}
