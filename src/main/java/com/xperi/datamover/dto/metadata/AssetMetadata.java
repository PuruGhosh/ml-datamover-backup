package com.xperi.datamover.dto.metadata;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class AssetMetadata {
  private String asset_uuid;
  private String subJobId;
  private Operations operation;
  private String meta_file_name;
  private String parameters;
  private Map<String, String> uiPartMetadata;
  private String contentAssetMetadata;
  private File filePartMetadata;
  private Store storePartMetadata;
  private List<String> userRoles;
}
