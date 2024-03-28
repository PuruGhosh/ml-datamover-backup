package com.xperi.datamover.dto;

import lombok.Data;

/** This is a DTO class for downloading asset */
@Data
public class AssetDownloadDto {

  private byte[] content;

  private String fileName;
}
