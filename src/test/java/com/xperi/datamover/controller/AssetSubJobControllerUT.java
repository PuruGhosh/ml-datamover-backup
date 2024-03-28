package com.xperi.datamover.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.dto.*;
import com.xperi.datamover.service.AssetJobService;
import com.xperi.datamover.service.AssetServiceClient;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.service.SearchServiceClient;
import com.xperi.datamover.util.DataMoverTestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** This class is used for unit testing Sub Job Controller */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public class AssetSubJobControllerUT {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired AssetSubJobController assetSubJobController;
  @MockBean private AssetJobService assetJobService;
  @MockBean private AssetSubJobService assetSubJobService;
  @MockBean private AssetServiceClient assetServiceClient;
  @MockBean private SearchServiceClient searchServiceClient;
  @Autowired private MockMvc mockMvc;

  @Test
  public void testAssetSubJobControllerNotNull() {
    assertThat(assetSubJobController).isNotNull();
  }

  /** Test method for retrieving all sub jobs by parent job id API */
  @Test
  @DisplayName("Testing retrieve all sub jobs by parent job id API")
  public void testRetrieveAllAssetSubJobsByParentJobId() throws Exception {

    Map<String, String> testMetaData = new HashMap<String, String>();
    testMetaData.put("Key1", "Value1");

    // dummy asset sub Job Dto object for retrieval
    final AssetSubJobDto assetSubJobDto = DataMoverTestUtil.createAssetSubJobDto();

    List<AssetSubJobDto> assetSubJobDtoList = new ArrayList<>();
    assetSubJobDtoList.add(assetSubJobDto);

    Pageable p = PageRequest.of(1, 5);
    Page<AssetSubJobDto> assetSubJobDtoPage = new PageImpl<>(assetSubJobDtoList);

    when(assetSubJobService.findByParentJobIdPageable("1a039a5c-7531-4e26-96a0-361f249de34f", p))
        .thenReturn(assetSubJobDtoPage);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/sub-jobs/1a039a5c-7531-4e26-96a0-361f249de34f?size=5&page=1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            jsonPath("$.data.content[0].status").value(assetSubJobDto.getStatus().toString()))
        .andExpect(jsonPath("$.data.content[0].parentJobId").value(assetSubJobDto.getParentJobId()))
        .andExpect(jsonPath("$.data.content[0].fileName[0]").value(assetSubJobDto.getFileName().get(0)))
        .andExpect(jsonPath("$.data.content[0].type").value(assetSubJobDto.getType().toString()));

  }

  /** Test method for retrieving all sub jobs with Complete status */
  @Test
  @DisplayName("Testing retrieve all Asset sub jobs in completed status API")
  public void testRetrieveAllAssetSubJobsWithCompleteStatus() throws Exception {

    // dummy Completed Asset Sub Job Dto object for retrieval
    final AssetSubJobDto assetSubJobDto = new AssetSubJobDto();
    final AssetSubJobResponseDto responseDto = new AssetSubJobResponseDto();

    assetSubJobDto.setFileName(List.of("testFileName.json"));
    assetSubJobDto.setUrl("testUrl");
    responseDto.setAssetList(Arrays.asList(assetSubJobDto));

    final AssetSubJobRequestDto assetSubJobRequestDto = new AssetSubJobRequestDto();
    assetSubJobRequestDto.setStatus(AssetJobStatus.COMPLETE);
    final String json = objectMapper.writeValueAsString(assetSubJobRequestDto);

    when(assetSubJobService.findAssetSubJobs(any(AssetSubJobRequestDto.class)))
        .thenReturn(responseDto);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/sub-jobs/retrieve")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.data.assetList[0].fileName[0]").value(assetSubJobDto.getFileName().get(0)))
        .andExpect(jsonPath("$.data.assetList[0].url").value(assetSubJobDto.getUrl()));
  }

  /** Test method for update bulk metadata */
  @Test
  @Disabled
  @DisplayName("Testing update bulk metadata API")
  public void testUpdateBulkMetadata() throws Exception {

    final RetrieveAssetJobDto retrieveAssetJobDto = DataMoverTestUtil.createRetrieveAssetJobDto();
    final String json = objectMapper.writeValueAsString(DataMoverTestUtil.createUpdateBulkMetadataRequestDto());

    when(assetSubJobService.updateBulkMetadata(any(UpdateBulkMetadataRequestDto.class), any(List.class)))
            .thenReturn(retrieveAssetJobDto);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.put("/sub-jobs/updateBulkMetadata")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data.ownerId").value(retrieveAssetJobDto.getOwnerId()))
            .andExpect(jsonPath("$.data.status").value(retrieveAssetJobDto.getStatus().toString()))
            .andExpect(jsonPath("$.data.jobName").value(retrieveAssetJobDto.getJobName()))
            .andExpect(jsonPath("$.data.description").value(retrieveAssetJobDto.getDescription()));
  }
}
