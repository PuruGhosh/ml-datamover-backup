package com.xperi.datamover.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.dto.AssetDataLoaderDto;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.service.AssetJobService;
import com.xperi.datamover.service.AssetServiceClient;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.service.SearchServiceClient;
import com.xperi.datamover.util.DataMoverTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** This class is used for unit testing Asset Data Loader Controller */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public class AssetDataLoaderControllerUT {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired AssetDataLoaderController dataLoaderController;
  @MockBean private AssetJobService assetJobService;
  @MockBean private AssetSubJobService assetSubJobService;
  @MockBean private AssetServiceClient assetServiceClient;
  @MockBean private SearchServiceClient searchServiceClient;
  @Autowired private MockMvc mockMvc;

  @Test
  public void testAssetDataLoaderControllerNotNull() {
    assertThat(dataLoaderController).isNotNull();
  }

  @Test
  @DisplayName("Testing retrieve all asset sub jobs by list of asset sub jobs ids API")
  public void testFindAssetsByIds() throws Exception {
    final AssetSubJobDto assetSubJobDto = DataMoverTestUtil.createAssetSubJobDto();
    List<AssetSubJobDto> assetSubJobDtos = new ArrayList<>();
    assetSubJobDtos.add(assetSubJobDto);

    when(assetSubJobService.findAllByIds(any(List.class))).thenReturn(assetSubJobDtos);

    final AssetDataLoaderDto assetDataLoaderDto = new AssetDataLoaderDto();
    List<String> assetIds = new ArrayList<>();
    assetIds.add(assetSubJobDto.getId());
    assetDataLoaderDto.setAssetIds(assetIds);

    final String json = objectMapper.writeValueAsString(assetDataLoaderDto);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/data-loader/assets")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.data[0].status").value(assetSubJobDto.getStatus().toString()))
        .andExpect(jsonPath("$.data[0].fileName[0]").value(assetSubJobDto.getFileName().get(0)))
        .andExpect(jsonPath("$.data[0].type").value(assetSubJobDto.getType().toString()));

  }
}
