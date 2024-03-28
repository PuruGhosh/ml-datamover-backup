package com.xperi.datamover.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.exception.DataMoverException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** This class contains different utility methods for Job services. */
@Slf4j
public class AssetJobUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * To list all the files inside a folder path. The listing is not recursive.
   *
   * @param path
   * @return
   */
  public static List<String> listFilesFromDir(Path path) {
    try (var walk = Files.walk(path)) {
      return walk.filter(Files::isRegularFile)
                 .map(Path::toString)
                 .collect(Collectors.toList());
    } catch (NoSuchFileException e) {
      log.error("Folder '{}' does not exist", path);
      throw new DataMoverException("Folder does not exist: " + path);
    } catch (Exception e) {
      log.error("Folder '{}' listing error", path, e);
      throw new DataMoverException("Folder listing error: " + path);
    }
  }

  /**
   * This method converts json file contents(byte array) to a map object
   *
   * @param content Content of the json file in byte array
   * @return A map object
   */
  public static Map<String, String> toMap(byte[] content) {
    TypeReference<Map<String, String>> typeReference = new TypeReference<>() {};
    try {
      Map<String, String> value = objectMapper.readValue(content, typeReference);
      return value;
    } catch (Exception e) {
      log.error("Unable to convert to Map. Details {} ", e.getLocalizedMessage(), e);
      return null;
    }
  }
}
