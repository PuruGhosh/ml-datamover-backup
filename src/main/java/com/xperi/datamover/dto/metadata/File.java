package com.xperi.datamover.dto.metadata;

import lombok.Data;

import java.util.List;

@Data
public class File {
  private List<String> file_name;
  private String meta_file_name;
  private String owner;
  private String metadata;
  private MetadataType metadata_type;
}
