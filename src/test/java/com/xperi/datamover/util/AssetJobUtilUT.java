package com.xperi.datamover.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssetJobUtilUT {

  private static final AssetJobUtil assetJobUtil = new AssetJobUtil();

  @Test
  @DisplayName("Test Asset files count in the nested folder")
  public void testAssetFileCountInNestedFolder() throws Exception {
    var srcPath = DataMoverTestUtil.getFileFromClasspath("composed_assets_with_nested_dir");
    var listOfAssets = assetJobUtil.listFilesFromDir(srcPath);
    assertEquals(6, listOfAssets.stream().count(), "Asset count is not matching");
  }

  @Test
  @DisplayName("Test Asset files count in the normal folder")
  public void testAssetFileCountInFolder() throws Exception {
    var srcPath = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-1");
    var listOfAssets = assetJobUtil.listFilesFromDir(srcPath);
    assertEquals(4, listOfAssets.stream().count(), "Asset count is not matching");
  }
}
