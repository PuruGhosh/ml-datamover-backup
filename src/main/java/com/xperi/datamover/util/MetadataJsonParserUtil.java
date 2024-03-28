package com.xperi.datamover.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.dto.metadata.MetadataType;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class MetadataJsonParserUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Data
  public static class MetadataHeader {
    private String metaFileHash;
    private String metaFileName ;
    private Map<String,String> parameters = new HashMap<>();
  }
  public static MetadataHeader readHeader(Path filePath) throws IOException {
    return readMetadata(filePath, null);
  }


  public static void readFileParts(Path filePath, Predicate<com.xperi.datamover.dto.metadata.File> callback) throws IOException {
    readMetadata(filePath, Objects.requireNonNull(callback));
  }


  private static MetadataHeader readMetadata(Path filePath, Predicate<com.xperi.datamover.dto.metadata.File> callback) throws IOException {
    var jsonFactory = new JsonFactory();
    var header = new MetadataHeader();

    outerLabel:
    try (var jp = jsonFactory.createParser(Files.newInputStream(filePath))) {
      while (jp.nextToken() != JsonToken.END_OBJECT) {
        String currentName = jp.getCurrentName();
        if(JsonToken.FIELD_NAME == jp.currentToken()) {
          jp.nextToken();
          switch (currentName) {
            case "meta-file-name" -> header.setMetaFileName(jp.getText());
            case "parameters" -> parseParameter(jp,header.getParameters(), callback);
            case "files" -> {
              if( !parseFiles(header,jp,callback) ) {
                break outerLabel; //do not read all the file in some cases
              }
            }
          }
        }
      }
    }
    return header;
  }

  private static boolean parseFiles(MetadataHeader header, JsonParser jp,
                                    Predicate<com.xperi.datamover.dto.metadata.File> callback) throws IOException {
    if(callback == null) {
      if(header.getMetaFileName() != null && header.getMetaFileHash() != null) {
        return false; //do not read all the file if all required data has been read.
      }
      MAPPER.readTree(jp); //not interested, skip all
    }
    else if(JsonToken.START_ARRAY == jp.currentToken()) {
      while (jp.nextToken() != JsonToken.END_ARRAY) {
        if(JsonToken.START_OBJECT == jp.currentToken()) {
          var assetFile = new com.xperi.datamover.dto.metadata.File();
          while (jp.nextToken() != JsonToken.END_OBJECT) {
            String currentName = jp.getCurrentName();
            if(JsonToken.FIELD_NAME == jp.currentToken()) {
              jp.nextToken();
              switch (currentName) {
                case "file-name" -> assetFile.setFile_name(readFileNameArray(jp));
                case "meta-file-name" -> assetFile.setMeta_file_name(jp.getText());
                case "owner" -> assetFile.setOwner(jp.getText());
                case "metadata" -> assetFile.setMetadata(MAPPER.readTree(jp).toString());
                case "metadata_type"-> assetFile.setMetadata_type(MetadataType.valueOf(jp.getText()));
              }
            }
          }
          if( !callback.test(assetFile) ) {
            return false; //stop early. Error.
          }
        }
      }
    }

    return true;
  }

  private static void parseParameter(JsonParser jp, Map<String,String> targetMap,
                                     Predicate<com.xperi.datamover.dto.metadata.File> callback) throws IOException {
    if(callback == null) {
      while (jp.nextToken() != JsonToken.END_OBJECT) {
        String currentName = jp.getCurrentName();
        if(JsonToken.FIELD_NAME == jp.currentToken()) {
          jp.nextToken();
          String val = jp.getText();
          if(JsonToken.START_OBJECT == jp.currentToken()) {
            val = MAPPER.readTree(jp).toString();
          }
          targetMap.put(currentName,val);
        }
      }
    }
    else {
      MAPPER.readTree(jp); //not interested. skip all
    }
  }

  private static List<String> readFileNameArray(JsonParser jp) throws IOException {
    List<String> fileNames= new ArrayList<>();
    // Check if it's the start of an array
    if (JsonToken.START_ARRAY.equals(jp.currentToken())) {
      // Loop through the array elements
      while (jp.nextToken() != JsonToken.END_ARRAY) {
        // Get the array element value
        var fileName = jp.getText();
        fileNames.add(fileName);
      }
    }
    return fileNames;
  }
}
