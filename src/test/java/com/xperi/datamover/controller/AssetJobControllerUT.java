package com.xperi.datamover.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.dto.MetadataJobRequestDto;
import com.xperi.datamover.dto.RetrieveAssetJobDto;
import com.xperi.datamover.service.AssetJobService;
import com.xperi.datamover.service.AssetServiceClient;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.service.SearchServiceClient;
import com.xperi.datamover.util.DataMoverTestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** This class is used for unit testing Job Controller */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
public class AssetJobControllerUT {

  private static final String generatedMetaDataKey = RandomStringUtils.randomAlphabetic(65);
  private static final String generatedMetaDataValue = RandomStringUtils.randomAlphabetic(65);

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired AssetJobController assetJobController;
  @MockBean private AssetJobService assetJobService;
  @MockBean private AssetSubJobService assetSubJobService;
  @MockBean private AssetServiceClient assetServiceClient;
  @MockBean private SearchServiceClient searchServiceClient;
  @Autowired private MockMvc mockMvc;

  @Test
  public void testJobControllerNotNull() {
    assertThat(assetJobController).isNotNull();
  }

  @Test
  @Disabled
  @DisplayName("Testing create Job API when the request body matches all acceptance criteria")
  public void testCreateJob() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final String json = objectMapper.writeValueAsString(assetJobDto);
    when(assetJobService.create(any(AssetJobDto.class), any(List.class))).thenReturn(assetJobDto);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/jobs")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.data.ownerId").value(assetJobDto.getOwnerId()))
        .andExpect(jsonPath("$.data.status").value(assetJobDto.getStatus().toString()))
        .andExpect(jsonPath("$.data.message").value(assetJobDto.getMessage()))
        .andExpect(jsonPath("$.data.jobName").value(assetJobDto.getJobName()))
        .andExpect(jsonPath("$.data.description").value(assetJobDto.getDescription()))
        .andExpect(
            jsonPath("$.data.assetLocationType")
                .value(assetJobDto.getAssetLocationType().toString()))
        .andExpect(jsonPath("$.data.metaData").value(assetJobDto.getMetaData()));
  }

  /**
   * @param assetJobDto
   * @throws Exception
   */
  private void performDataValidationError(AssetJobDto assetJobDto) throws Exception {

    final String json = objectMapper.writeValueAsString(assetJobDto);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/jobs")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(jsonPath("$.error").value(true));
  }

  // negative test case -start
  @Test
  @DisplayName("Testing create job API when job name is blank")
  public void testCreateJob_whenJobNameIsBlank() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setJobName(StringUtils.EMPTY);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when description is blank")
  public void testCreateJob_whenDescriptionIsBlank() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setDescription(StringUtils.EMPTY);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when location(s) is empty")
  public void testCreateJob_whenLocationsIsEmpty() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setLocations(new ArrayList<>(0));
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when description length is above 2048")
  public void testCreateJob_whenDescriptionLengthAbove2048() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setDescription(RandomStringUtils.randomAlphabetic(2049));
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when metadata key length is more than 256 characters")
  public void testCreateJob_whenMetaDataKeyLengthIsAbove256() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final Map<String, String> metaData = new HashMap<>();
    metaData.put(RandomStringUtils.randomAlphabetic(257), "testValue");
    assetJobDto.setMetaData(metaData);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when metadata value length is more than 1024 characters")
  public void testCreateJob_whenMetaDataValueLengthIsAbove1024() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", RandomStringUtils.randomAlphabetic(1025));
    assetJobDto.setMetaData(metaData);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when meta data file is not of json type")
  public void testCreateJob_whenMetaDataFileNotJson() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setMetaDataFileName("testMetaData.txt");
    performDataValidationError(assetJobDto);
  }

  // negative test case -end
  @Test
  @DisplayName("Testing create job API when meta data file is not of json type")
  public void testCreateJob_whenLocationInvalid() throws Exception {

    AssetJobDto assetJobDto = new AssetJobDto();
    Map<String, String> metaData = new HashMap<>();
    List<String> fileNames = new ArrayList<>();

    fileNames.add("file1");
    fileNames.add("file2");
    metaData.put("testKey", "testValue");

    assetJobDto.setOwnerId("testOwner");
    assetJobDto.setMessage("testMessage");
    assetJobDto.setJobName("testJob");
    assetJobDto.setDescription("testDescription");
    assetJobDto.setAssetLocationType(AssetLocationType.FILE);
    assetJobDto.setLocations(fileNames);
    assetJobDto.setMetaData(metaData);
    assetJobDto.setMetaDataFileName("testMetaData.txt");

    performDataValidationError(assetJobDto);
  }

  /** Test method for retrieving all Asset Group jobs API */
  @Test
  @DisplayName("Testing retrieve all Asset Group jobs API")
  public void testRetrieveAllAssetGroupJobs() throws Exception {

    // dummy Retrieve Asset Job Dto object
    final RetrieveAssetJobDto retrieveJobDto = new RetrieveAssetJobDto();
    retrieveJobDto.setOwnerId("testOwnerId");
    retrieveJobDto.setCreatedAt(Calendar.getInstance().getTime());
    retrieveJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    retrieveJobDto.setJobName("testJob");
    retrieveJobDto.setDescription("testDescription");
    retrieveJobDto.setFolder("testFolder");
    retrieveJobDto.setInitialMetadataFile(true);
    Map<AssetJobStatus, Long> jobStatus = new HashMap<AssetJobStatus, Long>();
    jobStatus.put(AssetJobStatus.COMPLETE, Long.valueOf(1));
    jobStatus.put(AssetJobStatus.ERROR, Long.valueOf(1));
    jobStatus.put(AssetJobStatus.IN_PROGRESS, Long.valueOf(1));
    retrieveJobDto.setSubJobStatus(jobStatus);
    List<RetrieveAssetJobDto> testJobList = new ArrayList<>();
    testJobList.add(retrieveJobDto);

    Pageable p = PageRequest.of(1, 5);
    Page<RetrieveAssetJobDto> page = new PageImpl<>(testJobList);

    when(assetJobService.retrieve(p)).thenReturn(page);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/jobs?size=5&page=1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.data.content[0].ownerId").value(retrieveJobDto.getOwnerId()))
        .andExpect(
            jsonPath("$.data.content[0].status").value(retrieveJobDto.getStatus().toString()))
        .andExpect(jsonPath("$.data.content[0].jobName").value(retrieveJobDto.getJobName()))
        .andExpect(jsonPath("$.data.content[0].description").value(retrieveJobDto.getDescription()))
        .andExpect(jsonPath("$.data.content[0].folder").value(retrieveJobDto.getFolder()))
        .andExpect(
            jsonPath("$.data.content[0].initialMetadataFile")
                .value(retrieveJobDto.isInitialMetadataFile()))
        .andExpect(jsonPath("$.data.content[0].subJobStatus.COMPLETE").value(1))
        .andExpect(jsonPath("$.data.content[0].subJobStatus.ERROR").value(1))
        .andExpect(jsonPath("$.data.content[0].subJobStatus.IN_PROGRESS").value(1));
  }

  /** Test method for retrieving Asset job by id API */
  @Test
  @DisplayName("Testing retrieve Asset job by id API")
  public void testRetrieveAssetJobById() throws Exception {

    // dummy Retrieve Asset Job Dto object
    final RetrieveAssetJobDto retrieveJobDto = new RetrieveAssetJobDto();
    retrieveJobDto.setId(UUID.randomUUID().toString());
    retrieveJobDto.setOwnerId("testOwnerId");
    retrieveJobDto.setCreatedAt(Calendar.getInstance().getTime());
    retrieveJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
    retrieveJobDto.setJobName("testJob");
    retrieveJobDto.setDescription("testDescription");
    retrieveJobDto.setFolder("testFolder");
    retrieveJobDto.setInitialMetadataFile(true);
    Map<AssetJobStatus, Long> jobStatus = new HashMap<AssetJobStatus, Long>();
    jobStatus.put(AssetJobStatus.COMPLETE, Long.valueOf(1));
    jobStatus.put(AssetJobStatus.ERROR, Long.valueOf(1));
    jobStatus.put(AssetJobStatus.IN_PROGRESS, Long.valueOf(1));
    retrieveJobDto.setSubJobStatus(jobStatus);


    when(assetJobService.retrieveAssetJobById(any(UUID.class))).thenReturn(retrieveJobDto);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.get("/jobs/8a2fb110-6ef5-42d5-86d8-79be52e4bd52")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data.ownerId").value(retrieveJobDto.getOwnerId()))
            .andExpect(jsonPath("$.data.status").value(retrieveJobDto.getStatus().toString()))
            .andExpect(jsonPath("$.data.jobName").value(retrieveJobDto.getJobName()))
            .andExpect(jsonPath("$.data.description").value(retrieveJobDto.getDescription()))
            .andExpect(jsonPath("$.data.folder").value(retrieveJobDto.getFolder()))
            .andExpect(
                    jsonPath("$.data.initialMetadataFile")
                            .value(retrieveJobDto.isInitialMetadataFile()))
            .andExpect(jsonPath("$.data.subJobStatus.COMPLETE").value(1))
            .andExpect(jsonPath("$.data.subJobStatus.ERROR").value(1))
            .andExpect(jsonPath("$.data.subJobStatus.IN_PROGRESS").value(1));
  }

  @Test
  @DisplayName("Testing create Metadata Job API when the request body matches all acceptance criteria")
  public void testCreateMetadataJob() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    final MetadataJobRequestDto metadataJobRequestDto = DataMoverTestUtil.createMetadataJobRequestDto();
    final String json = objectMapper.writeValueAsString(metadataJobRequestDto);
    when(assetJobService.createMetadataJob(any(MetadataJobRequestDto.class))).thenReturn(assetJobDto);

    mockMvc
            .perform(
                    MockMvcRequestBuilders.post("/jobs/metadata")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.data.ownerId").value(assetJobDto.getOwnerId()))
            .andExpect(jsonPath("$.data.status").value(assetJobDto.getStatus().toString()))
            .andExpect(jsonPath("$.data.message").value(assetJobDto.getMessage()))
            .andExpect(jsonPath("$.data.jobName").value(assetJobDto.getJobName()))
            .andExpect(jsonPath("$.data.description").value(assetJobDto.getDescription()));
  }

  @Test
  @DisplayName("Testing create Metadata job API when description is Blank")
  public void testCreateMetadataJob_whenDescriptionIsBlank() throws Exception {
    final MetadataJobRequestDto metadataJobRequestDto = DataMoverTestUtil.createMetadataJobRequestDto();
    metadataJobRequestDto.setDescription(StringUtils.EMPTY);
    final String json = objectMapper.writeValueAsString(metadataJobRequestDto);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.post("/jobs/metadata")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(jsonPath("$.error").value(true));
  }


  @Test
  @DisplayName("Testing create Metadata job API when description is Null")
  public void testCreateMetadataJob_whenDescriptionIsNULL() throws Exception {
    final MetadataJobRequestDto metadataJobRequestDto = DataMoverTestUtil.createMetadataJobRequestDto();
    metadataJobRequestDto.setDescription(null);
    final String json = objectMapper.writeValueAsString(metadataJobRequestDto);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.post("/jobs/metadata")
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(jsonPath("$.error").value(true));
  }

  @Test
  @DisplayName(
          "Testing existsByJobName throws 404 status if job name does not exist")
  void testExistsByJobNameThrowsOk() throws Exception {
    final String jobName = "testJobName";
    when(assetJobService.existsByJobName(anyString())).thenReturn(false);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.get("/jobs/existsByJobName/%s".formatted(jobName))
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
  }
  @Test
  @DisplayName(
          "Testing existsByJobName throws 200 status if job name exists")
  void testExistsByJobNameThrowsNotFound() throws Exception {
    final String jobName = "testJobName";
    when(assetJobService.existsByJobName(anyString())).thenReturn(true);
    mockMvc
            .perform(
                    MockMvcRequestBuilders.get("/jobs/existsByJobName/%s".formatted(jobName))
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
  }
}
