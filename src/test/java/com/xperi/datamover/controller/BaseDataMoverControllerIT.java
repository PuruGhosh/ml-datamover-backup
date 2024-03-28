package com.xperi.datamover.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.dto.AssetJobDto;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.model.minio.AssetObject;
import com.xperi.datamover.model.minio.Bucket;
import com.xperi.datamover.model.minio.BucketEvent;
import com.xperi.datamover.repository.AssetJobRepository;
import com.xperi.datamover.repository.AssetSubJobRepository;
import com.xperi.datamover.service.AssetSubJobService;
import com.xperi.datamover.util.DataMoverTestUtil;
import com.xperi.schema.subjobevent.AssetCategory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseDataMoverControllerIT {

  private static final int ASSETFILE_COUNT = 3;
  private final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Autowired AssetJobController assetJobController;

  @Autowired private Environment env;

  @Autowired private MockMvc mockMvc;

  @Autowired AssetSubJobService assetSubJobService;

  @Autowired AssetJobRepository assetJobRepository;

  @Autowired AssetSubJobRepository assetSubJobRepository;

  private static List<String> assetPathnameList;
  private static Path metadataFile;

  /**
   * create the dummy asset file
   *
   * @throws Exception
   */
  @BeforeAll
  static void createAssetFile() throws Exception {
    final String[] splitFileName =
        StringUtils.split(DataMoverTestUtil.TEMP_ASSET_FILE, DataMoverTestUtil.FILE_DOT_SEPARATOR);
    var list = new ArrayList<String>();
    for (int i = 0; i < ASSETFILE_COUNT; i++) {
      var tempFile =
          Files.createTempFile(
              splitFileName[0], "-" + i + DataMoverTestUtil.FILE_DOT_SEPARATOR + splitFileName[1]);
      Files.write(tempFile, "test".getBytes());
      list.add(tempFile.toAbsolutePath().toString());
    }
    assetPathnameList = List.copyOf(list);
  }

  /**
   * create a dummy metadata json file
   *
   * @throws Exception
   */
  @BeforeAll
  static void createMetadataFile() throws Exception {
    final String[] splitFileName =
        StringUtils.split(
            DataMoverTestUtil.TEMP_METADATA_FILE, DataMoverTestUtil.FILE_DOT_SEPARATOR);
    final Path tempFile =
        Files.createTempFile(
            splitFileName[0], DataMoverTestUtil.FILE_DOT_SEPARATOR + splitFileName[1]);
    Files.write(tempFile, "{key:value}".getBytes());
    metadataFile = tempFile;
  }

  // postive test case -start
  /**
   * Call the create controller without any metadatafile
   *
   * @throws Exception
   */
  @Test
  @DisplayName("Test Create Job via Controller w/o metadata file")
  @DirtiesContext
  public void testCreateJobAndSubJobWithoutMetadataFile() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setLocations(assetPathnameList);
    final String json = objectMapper.writeValueAsString(assetJobDto);
    // this is for AssetJob Creation
    final String parentJobId = checkCreationOfAssetJob(assetJobDto, json);
    final int expectedSubJobEntries = assetPathnameList.size();
    // SubJob creation is async
    TimeUnit.MILLISECONDS.sleep(500);
    checkCreationOfAssetSubJob(parentJobId, expectedSubJobEntries);

    var assetJob = assetJobRepository.findById(UUID.fromString(parentJobId));
    assertTrue(assetJob.isPresent());
    assertProgress(assetJob.get(), 3, 0, 0);
  }

  /**
   * Call the create controller with a metadatafile
   *
   * @throws Exception
   */
  @Test
  @DisplayName("Test Create Job via Controller with metadata")
  @DirtiesContext
  public void testCreateJobAndSubJobWithMetadataFile() throws Exception {

    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    // let us add the metadata file
    assetJobDto.setMetaDataFileName(metadataFile.toFile().getAbsolutePath());
    // add the asset file
    final List<String> fileNames = Collections.singletonList(assetPathnameList.get(0));
    assetJobDto.setLocations(fileNames);
    final String json = objectMapper.writeValueAsString(assetJobDto);
    // this is for AssetJob Creation
    final String parentJobId = checkCreationOfAssetJob(assetJobDto, json);
    // size is 2 as we are  adding  one asset and one metadata file
    final int expectedSubJobEntries = 2;
    // SubJob creation is async
    TimeUnit.MILLISECONDS.sleep(500);
    checkCreationOfAssetSubJob(parentJobId, expectedSubJobEntries);
  }

  // postive test case -end

  /**
   * util methof to check the creation of AssetJob entity
   *
   * @param assetJobDto
   * @param json
   * @throws Exception
   * @return jobId
   */
  private String checkCreationOfAssetJob(AssetJobDto assetJobDto, String json) throws Exception {
    final ResultActions resultActions =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/jobs")
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());

    final MvcResult result = resultActions.andReturn();
    final String jsonString = result.getResponse().getContentAsString();

    // getting the id that we created
    final String jobId = JsonPath.parse(jsonString).read("$.data.id").toString();

    resultActions
        .andExpect(jsonPath("$.data.id").hasJsonPath())
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.ownerId").value(assetJobDto.getOwnerId()))
        .andExpect(jsonPath("$.data.status").value(assetJobDto.getStatus().toString()))
        .andExpect(jsonPath("$.data.message").value(assetJobDto.getMessage()))
        .andExpect(jsonPath("$.data.jobName").value(assetJobDto.getJobName()))
        .andExpect(jsonPath("$.data.description").value(assetJobDto.getDescription()))
        .andExpect(
            jsonPath("$.data.assetLocationType")
                .value(assetJobDto.getAssetLocationType().toString()))
        .andExpect(jsonPath("$.data.metaData").value(assetJobDto.getMetaData()));

    return jobId;
  }

  /**
   * check if subjob has been created successfullly in mongo
   *
   * @param parentJobId
   * @param expectedSubJobEntries
   */
  private void checkCreationOfAssetSubJob(String parentJobId, int expectedSubJobEntries) {
    // get the subJob entries created
    final List<AssetSubJobDto> assetSubJobDtos = assetSubJobService.findByParentJobId(parentJobId);
    // check the size based on whether it is with metadata or not
    MatcherAssert.assertThat(assetSubJobDtos, hasSize(Matchers.equalTo(expectedSubJobEntries)));
    // some more prelim checks
    MatcherAssert.assertThat(
        assetSubJobDtos,
        Matchers.hasItem(
            Matchers.allOf(
                Matchers.hasProperty("parentJobId", Matchers.equalTo(parentJobId)),
                Matchers.hasProperty("id", Matchers.notNullValue()),
                Matchers.hasProperty("status", Matchers.equalTo(AssetJobStatus.IN_PROGRESS)))));
  }

  // negative test case -start
  @Test
  @DisplayName("Test Create Job validation fail for empty asset list")
  @DirtiesContext
  public void testCreateJobFailForEmptyAssetList() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setLocations(null);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when description is blank")
  @DirtiesContext
  public void testCreateJobFail_whenDescriptionIsBlank() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    // set the test data with empty string
    assetJobDto.setDescription(StringUtils.EMPTY);
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when description length is above 2048")
  @DirtiesContext
  public void testCreateJobFail_whenDescriptionLengthAbove2048() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    // set the test data with 2049 chars
    assetJobDto.setDescription(RandomStringUtils.randomAlphabetic(2049));
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing create job API when metadata file is not json")
  @DirtiesContext
  public void testCreateJobFail_whenMetadataFileIsNotJson() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setMetaDataFileName("metadata.png");
    performDataValidationError(assetJobDto);
  }

  @Test
  @DisplayName("Testing asset upload progress")
  @DirtiesContext
  public void testAssetUploadProgress() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setLocations(assetPathnameList);
    final String json = objectMapper.writeValueAsString(assetJobDto);
    // this is for AssetJob Creation
    final String parentJobId = checkCreationOfAssetJob(assetJobDto, json);

    // SubJob creation is async
    Awaitility.await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> subJobsCountMatches(parentJobId, assetJobDto.getLocations().size()));

    var assetJob = assetJobRepository.findById(UUID.fromString(parentJobId));
    assertTrue(assetJob.isPresent());
    assertProgress(assetJob.get(), 3, 0, 0);

    final List<AssetSubJobDto> assetSubJobDtos = assetSubJobService.findByParentJobId(parentJobId);

    for (int i = 0; i < assetSubJobDtos.size(); i++) {
      var subJobDto = assetSubJobDtos.get(i);
      sendMinioTransferDoneBucketEvent(
          subJobDto.getFileName().get(0), assetJobDto.getJobName(), parentJobId, subJobDto.getId());
      var subJobEntity = assetSubJobRepository.findById(UUID.fromString(subJobDto.getId()));
      assertEquals(AssetJobStatus.COMPLETE, subJobEntity.get().getStatus());

      // sendIndexingDoneBucketEvent(parentJobId, subJobDto.getId());
      assetJob = assetJobRepository.findById(UUID.fromString(parentJobId));
      assertProgress(assetJob.get(), 3, i + 1, 0);
    }
  }

  @Test
  @DisplayName("Testing handling MinIO event callback repetition")
  @DirtiesContext
  public void testMinIOTransferOKEventRepeated() throws Exception {
    final AssetJobDto assetJobDto = DataMoverTestUtil.createAssetJobDto(assetPathnameList.get(0));
    assertEquals(1, assetJobDto.getLocations().size());
    final String json = objectMapper.writeValueAsString(assetJobDto);
    // this is for AssetJob Creation
    final String parentJobId = checkCreationOfAssetJob(assetJobDto, json);

    // SubJob creation is async
    Awaitility.await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> subJobsCountMatches(parentJobId, assetJobDto.getLocations().size()));

    var assetJob = assetJobRepository.findById(UUID.fromString(parentJobId));
    assertTrue(assetJob.isPresent());
    assertProgress(assetJob.get(), 1, 0, 0);

    final var assetSubJobDtoList = assetSubJobService.findByParentJobId(parentJobId);
    assertEquals(1, assetJobDto.getLocations().size());

    var subJobDto = assetSubJobDtoList.get(0);
    sendMinioTransferDoneBucketEvent(
        subJobDto.getFileName().get(0), assetJobDto.getJobName(), parentJobId, subJobDto.getId());
    // repeat it
    sendMinioTransferDoneBucketEvent(
        subJobDto.getFileName().get(0), assetJobDto.getJobName(), parentJobId, subJobDto.getId());

    var subJobEntity = assetSubJobRepository.findById(UUID.fromString(subJobDto.getId()));
    // sendIndexingDoneBucketEvent(parentJobId, subJobDto.getId());
    // repeat it
    // sendIndexingDoneBucketEvent(parentJobId, subJobDto.getId());

    assetJob = assetJobRepository.findById(UUID.fromString(parentJobId));
    assertProgress(assetJob.get(), 1, 1, 0); // progress at 1 even if repeated
  }

  private boolean subJobsCountMatches(String parentJobId, int expectedSize) {
    return assetSubJobRepository.findByParentJobId(UUID.fromString(parentJobId)).size()
        == expectedSize;
  }

  private void assertProgress(
      AssetJobEntity assetJobEntity, long total, long progress, long failed) {
    var accumulatorMap = assetJobEntity.getSubJobStatusAccumulator();
    assertEquals(total, accumulatorMap.get(AssetJobStatusAccumulator.TOTAL));
    assertEquals(progress, accumulatorMap.get(AssetJobStatusAccumulator.PROGRESS_OK));
    assertEquals(failed, accumulatorMap.get(AssetJobStatusAccumulator.PROGRESS_FAILED));
  }

  private void sendMinioTransferDoneBucketEvent(
      String fileName, String jobName, String parentJobId, String subJobId) throws Exception {
    URL resource = getClass().getClassLoader().getResource("minio_sample_event.json");
    byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
    var bucketEventTemplate = objectMapper.readValue(resource, BucketEvent.class);

    bucketEventTemplate.setKey("/assets/" + fileName);
    Bucket bucket = bucketEventTemplate.getRecords().get(0).getS3().getBucket();
    AssetObject assetObject = bucketEventTemplate.getRecords().get(0).getS3().getObject();
    assetObject.getUserMetadata().put("X-Amz-Meta-Subjobid", subJobId);
    assetObject.getUserMetadata().put("X-Amz-Meta-Parentjobid", parentJobId);
    assetObject.getUserMetadata().put("X-Amz-Meta-Jobname", jobName);
    assetObject.getUserMetadata().put("X-Amz-Meta-Subjobtype", AssetCategory.ASSET.name());

    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(bucketEventTemplate);
  }

  //  private void sendIndexingDoneBucketEvent(String parentJobId, String subJobId) {
  //    var subJobIndexEvent = new SubJobIndexEvent();
  //    subJobIndexEvent.setIsIndexed(true);
  //    subJobIndexEvent.setSubJobId(subJobId);
  //    subJobIndexEvent.setParentJobId(parentJobId);
  //    assetSubJobService.updateSubJobStatusAccumulator(subJobIndexEvent);
  //  }

  /**
   * util method to test the data validation error
   *
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
        .andExpect(jsonPath("$.error").value(true))
        .andExpect((jsonPath("$.errors[*]").value("Data validation error")));
  }

  // negative test case -end
  /**
   * delete the dummy asset file and the dummy metadata file
   *
   * @throws Exception
   */
  @AfterAll
  void deleteAssetFile() throws Exception {
    for (String pathName : assetPathnameList) {
      Files.deleteIfExists(Paths.get(pathName));
    }
    Files.deleteIfExists(metadataFile);
  }
}
