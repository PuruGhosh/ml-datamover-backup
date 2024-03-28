package com.xperi.datamover.service;

import com.xperi.datamover.config.AssetConfigProperties;
import com.xperi.datamover.config.DataMoverConfigProperties;
import com.xperi.datamover.config.FilenameFilterConfig;
import com.xperi.datamover.config.ThreadPoolConfig;
import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.constants.AssetLocationType;
import com.xperi.datamover.dto.*;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.exception.DataMoverException;
import com.xperi.datamover.repository.AssetJobRepository;
import com.xperi.datamover.repository.AssetSubJobRepository;
import com.xperi.datamover.repository.MongoDBOperationsRepository;
import com.xperi.datamover.task.JobExecutorService;
import com.xperi.datamover.task.TaskQueue;
import com.xperi.datamover.task.TaskRejectedExecutionHandler;
import com.xperi.datamover.util.AssetJobMapper;
import com.xperi.datamover.util.AssetSubJobMapper;
import com.xperi.datamover.util.DataMoverTestUtil;
import com.xperi.datamover.util.KafkaUtil;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** This class is used for unit testing Job Service */
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@EnableConfigurationProperties(value = DataMoverConfigProperties.class)
@TestPropertySource("classpath:application.properties")
public class AssetSubJobServiceUT {

  private static final int INITIAL_SEMANTIC_VERSION = 1;
  private final AssetSubJobMapper assetSubJobMapper = Mappers.getMapper(AssetSubJobMapper.class);
  private final AssetJobMapper assetJobMapper = Mappers.getMapper(AssetJobMapper.class);
  @InjectMocks AssetSubJobService assetSubJobService;
  @Mock private AssetSubJobRepository assetSubJobRepository;
  @Mock private AssetJobRepository assetJobRepository;
  @Mock private KafkaProducer kafkaProducer;
  @Autowired private DataMoverConfigProperties configProperties;
  @Mock private AssetConfigProperties assetConfigProperties;
  private KafkaUtil kafkaUtil = new KafkaUtil();
  @Mock private MongoTemplate mongoTemplate;
  @Mock private MongoDBOperationsRepository mongoDBOperationsRepository;
  @Mock private FilenameFilterConfig filenameFilterConfig;
  private JobExecutorService jobExecutorService;

  @BeforeEach
  void createTestClass() {
    jobExecutorService = createJobExecutorService();
    assetSubJobService =
        new AssetSubJobService(
            assetSubJobRepository,
            assetJobRepository,
            assetSubJobMapper,
            assetJobMapper,
            kafkaProducer,
            configProperties,
            assetConfigProperties,
            kafkaUtil,
            mongoTemplate,
            jobExecutorService,
            mongoDBOperationsRepository,
            filenameFilterConfig);
  }

  public JobExecutorService createJobExecutorService() {
    ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
    BlockingQueue<Runnable> taskQueue = new TaskQueue<>();
    TaskRejectedExecutionHandler handler = new TaskRejectedExecutionHandler();
    CustomizableThreadFactory factory =
        new CustomizableThreadFactory(threadPoolConfig.getThreadNamePrefix());
    return new JobExecutorService(threadPoolConfig, taskQueue, factory, handler);
  }

  @Test
  @DisplayName("Testing create sub job method")
  public void testCreateSubJob() {
    UUID id = UUID.randomUUID();
    AssetSubJobDto assetSubJobDto = new AssetSubJobDto();
    Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", "testValue");

    assetSubJobDto.setParentJobId(UUID.randomUUID().toString());
    assetSubJobDto.setFileName(List.of("test"));
    assetSubJobDto.setType(AssetCategory.ASSET);
    // assetSubJobDto.setMetaData(metaData);

    AssetSubJobEntity assetSubJobEntity = assetSubJobMapper.toAssetSubJobEntity(assetSubJobDto);
    assetSubJobEntity.setId(id);
    when(assetSubJobRepository.save(any(AssetSubJobEntity.class))).thenReturn(assetSubJobEntity);

    String subJobId = assetSubJobService.create(assetSubJobDto);
    verify(assetSubJobRepository, times(1)).save(any(AssetSubJobEntity.class));

    assertEquals(id, UUID.fromString(subJobId), "Id is not equal");
  }

  @Test
  @DisplayName("Testing create sub job method when job details are empty")
  public void testCreateSubJob_whenRequestDtoIsBlank() {
    AssetSubJobDto assetSubJobDto = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          assetSubJobService.create(assetSubJobDto);
        });
  }

  @Test
  @DisplayName("Testing find sub jobs method when parent job is empty")
  public void testFindByParentJobId_whenParentJobIdNull() {
    String parentJobId = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          assetSubJobService.findByParentJobId(parentJobId);
        });
  }

  @Test
  @DisplayName("Testing find sub jobs method when parent job id")
  public void testFindByParentJobId() {

    Map<String, String> testMetaData = new HashMap<String, String>();
    testMetaData.put("Key1", "Value1");

    // dummy asset sub Job entity object for retrieval
    final AssetSubJobEntity assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(UUID.randomUUID());
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setParentJobId(UUID.randomUUID());
    assetSubJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobEntity.setFileName(List.of("test.png"));
    assetSubJobEntity.setType(AssetCategory.ASSET);
    // assetSubJobEntity.setMetaData(testMetaData);

    List<AssetSubJobEntity> assetSubJobEntities = new ArrayList<>();
    assetSubJobEntities.add(assetSubJobEntity);

    // dummy asset Job entity object for retrieval
    final AssetJobEntity assetJobEntity = new AssetJobEntity();
    assetJobEntity.setId(UUID.randomUUID());
    assetJobEntity.setOwnerId("testOwnerId");
    assetJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetJobEntity.setJobName("testJobName");

    when(assetJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(assetJobEntity));
    when(assetSubJobRepository.findByParentJobId(any(UUID.class))).thenReturn(assetSubJobEntities);

    List<AssetSubJobDto> result =
        assetSubJobService.findByParentJobId(UUID.randomUUID().toString());

    verify(assetJobRepository, times(1)).findById(any(UUID.class));

    verify(assetSubJobRepository, times(1)).findByParentJobId(any(UUID.class));

    assertEquals(
        assetSubJobEntity.getId(), UUID.fromString(result.get(0).getId()), "Id is not equal");
    assertEquals(assetSubJobEntity.getStatus(), result.get(0).getStatus(), "status is not equal");
    assertEquals(
        assetSubJobEntity.getCreatedAt(), result.get(0).getCreatedAt(), "createdAt is not equal");
    assertEquals(
        assetSubJobEntity.getParentJobId(),
        UUID.fromString(result.get(0).getParentJobId()),
        "parentJobId is equal");
    assertEquals(
        assetSubJobEntity.getFileName(), result.get(0).getFileName(), "file name is not equal");
    assertEquals(assetSubJobEntity.getType(), result.get(0).getType(), "type is not equal");
    // assertEquals(assetSubJobEntity.getMetaData(), result.get(0).getMetaData(), "metadata is not
    // equal");
  }

  @Test
  @DisplayName("Testing find pageable sub jobs method when parent job is empty")
  public void testFindByParentJobIdPageable_whenParentJobIdNull() {
    String parentJobId = null;
    Pageable p = PageRequest.of(1, 5);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          assetSubJobService.findByParentJobIdPageable(parentJobId, p);
        });
  }

  @Test
  @DisplayName(
      "Testing find pageable sub jobs using parent job Id and status when parent job is empty")
  public void testFindByParentJobIdAndStatusPageable_whenParentJobIdNull() {
    String parentJobId = null;
    String status = "ERROR";
    Pageable p = PageRequest.of(1, 5);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          assetSubJobService.findByParentJobIdAndStatusPageable(parentJobId, status, p);
        });
  }

  @Test
  @DisplayName("Testing find pageable sub jobs using parent job Id and status when status is null")
  public void testFindByParentJobIdAndStatusPageable_whenStatusNull() {
    String parentJobId = UUID.randomUUID().toString();
    String status = null;
    Pageable p = PageRequest.of(1, 5);

    assertThrows(
        DataMoverException.class,
        () -> {
          assetSubJobService.findByParentJobIdAndStatusPageable(parentJobId, status, p);
        });
  }

  @Test
  @DisplayName("Testing find pageable sub jobs method when parent job id")
  public void testFindByParentJobIdPageable() {
    Map<String, String> testMetaData = new HashMap<String, String>();
    testMetaData.put("Key1", "Value1");

    // dummy asset sub Job entity object for retrieval
    final AssetSubJobEntity assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(UUID.randomUUID());
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setParentJobId(UUID.randomUUID());
    assetSubJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobEntity.setFileName(List.of("test.png"));
    assetSubJobEntity.setType(AssetCategory.ASSET);
    // assetSubJobEntity.setMetaData(testMetaData);

    List<AssetSubJobEntity> assetSubJobEntities = new ArrayList<>();
    assetSubJobEntities.add(assetSubJobEntity);

    // dummy asset Job entity object for retrieval
    final AssetJobEntity assetJobEntity = new AssetJobEntity();
    assetJobEntity.setId(UUID.randomUUID());
    assetJobEntity.setOwnerId("testOwnerId");
    assetJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetJobEntity.setJobName("testJobName");

    Pageable p = PageRequest.of(5, 20);
    Page<AssetSubJobEntity> assetSubJobEntityPage = new PageImpl<>(assetSubJobEntities);

    when(assetJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(assetJobEntity));
    when(assetSubJobRepository.findByParentJobId(any(UUID.class), any(Pageable.class)))
        .thenReturn(assetSubJobEntityPage);

    Page<AssetSubJobDto> result =
        assetSubJobService.findByParentJobIdPageable(UUID.randomUUID().toString(), p);

    verify(assetJobRepository, times(1)).findById(any(UUID.class));

    verify(assetSubJobRepository, times(1)).findByParentJobId(any(UUID.class), any(Pageable.class));

    assertEquals(
        assetSubJobEntity.getId(),
        UUID.fromString(result.toList().get(0).getId()),
        "Id is not equal");
    assertEquals(
        assetSubJobEntity.getStatus(), result.toList().get(0).getStatus(), "status is not equal");
    assertEquals(
        assetSubJobEntity.getCreatedAt(),
        result.toList().get(0).getCreatedAt(),
        "createdAt is not equal");
    assertEquals(
        assetSubJobEntity.getParentJobId(),
        UUID.fromString(result.toList().get(0).getParentJobId()),
        "parentJobId is equal");
    assertEquals(
        assetSubJobEntity.getFileName(),
        result.toList().get(0).getFileName(),
        "file name is not equal");
    assertEquals(
        assetSubJobEntity.getType(), result.toList().get(0).getType(), "type is not equal");
    // assertEquals(assetSubJobEntity.getMetaData(), result.toList().get(0).getMetaData(), "metadata
    // is not equal");
  }

  @Test
  @DisplayName("Testing find pageable sub jobs using parent job id and status")
  public void testFindByParentJobIdAndStatusPageable() {
    String parentJobId = UUID.randomUUID().toString();
    String status = "ERROR";

    Map<String, String> testMetaData = new HashMap<String, String>();
    testMetaData.put("Key1", "Value1");

    // dummy asset sub Job entity object for retrieval
    final AssetSubJobEntity assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(UUID.randomUUID());
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setParentJobId(UUID.fromString(parentJobId));
    assetSubJobEntity.setStatus(AssetJobStatus.ERROR);
    assetSubJobEntity.setFileName(List.of("test.png"));
    assetSubJobEntity.setType(AssetCategory.ASSET);

    List<AssetSubJobEntity> assetSubJobEntities = new ArrayList<>();
    assetSubJobEntities.add(assetSubJobEntity);

    // dummy asset Job entity object for retrieval
    final AssetJobEntity assetJobEntity = new AssetJobEntity();
    assetJobEntity.setId(UUID.fromString(parentJobId));
    assetJobEntity.setOwnerId("testOwnerId");
    assetJobEntity.setStatus(AssetJobStatus.ERROR);
    assetJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetJobEntity.setJobName("testJobName");

    Pageable p = PageRequest.of(5, 20);
    Page<AssetSubJobEntity> assetSubJobEntityPage = new PageImpl<>(assetSubJobEntities);

    when(assetJobRepository.findById(any(UUID.class))).thenReturn(Optional.of(assetJobEntity));
    when(assetSubJobRepository.findByParentJobIdAndStatus(
            any(UUID.class), any(AssetJobStatus.class), any(Pageable.class)))
        .thenReturn(assetSubJobEntityPage);

    Page<AssetSubJobDto> result =
        assetSubJobService.findByParentJobIdAndStatusPageable(parentJobId, status, p);

    verify(assetJobRepository, times(1)).findById(UUID.fromString(parentJobId));
    verify(assetSubJobRepository, times(1))
        .findByParentJobIdAndStatus(
                UUID.fromString(parentJobId), AssetJobStatus.ERROR, p);

    assertEquals(
        assetSubJobEntity.getId(),
        UUID.fromString(result.toList().get(0).getId()),
        "Id is not equal");
    assertEquals(
        assetSubJobEntity.getStatus(), result.toList().get(0).getStatus(), "status is not equal");
    assertEquals(
        assetSubJobEntity.getCreatedAt(),
        result.toList().get(0).getCreatedAt(),
        "createdAt is not equal");
    assertEquals(
        assetSubJobEntity.getParentJobId(),
        UUID.fromString(result.toList().get(0).getParentJobId()),
        "parentJobId is equal");
    assertEquals(
        assetSubJobEntity.getFileName(),
        result.toList().get(0).getFileName(),
        "file name is not equal");
    assertEquals(
        assetSubJobEntity.getType(), result.toList().get(0).getType(), "type is not equal");
  }

  @Test
  @DisplayName("Testing find Asset jobs in completed status")
  public void testFindCompletedSubJobs() {

    // dummy asset sub Job entity object as in complete for retrieval
    final AssetSubJobEntity completeAssetSubJobEntity = new AssetSubJobEntity();
    completeAssetSubJobEntity.setStatus(AssetJobStatus.COMPLETE);
    completeAssetSubJobEntity.setFileName(List.of("test1.png"));
    completeAssetSubJobEntity.setType(AssetCategory.ASSET);
    completeAssetSubJobEntity.setUrl("testURL");
    completeAssetSubJobEntity.setCreatedBy("TestUser");

    List<AssetSubJobEntity> assetSubJobEntities = new ArrayList<>();
    assetSubJobEntities.add(completeAssetSubJobEntity);
    Page<AssetSubJobEntity> entityPage =
        new PageImpl<AssetSubJobEntity>(
            assetSubJobEntities, PageRequest.of(0, 1), assetSubJobEntities.size());

    when(assetSubJobRepository.findByStatusAndType(
            any(AssetJobStatus.class), any(AssetCategory.class), any(Pageable.class)))
        .thenReturn(entityPage);

    final AssetSubJobRequestDto dto = new AssetSubJobRequestDto();
    dto.setStatus(AssetJobStatus.COMPLETE);

    AssetSubJobResponseDto result = assetSubJobService.findAssetSubJobs(dto);

    verify(assetSubJobRepository, times(1))
        .findByStatusAndType(
            any(AssetJobStatus.class), any(AssetCategory.class), any(Pageable.class));

    assertEquals(
        assetSubJobEntities.size(),
        result.getAssetList().size(),
        "Completed sub-job count is not equal");
    assertEquals(
        completeAssetSubJobEntity.getFileName(),
        result.getAssetList().get(0).getFileName(),
        "File name is not equal");
    assertEquals(
        completeAssetSubJobEntity.getUrl(),
        result.getAssetList().get(0).getUrl(),
        "url is not equal");
    assertEquals(
        completeAssetSubJobEntity.getCreatedBy(),
        result.getAssetList().get(0).getCreatedBy(),
        "created by is not equal");
  }

  @Test
  @DisplayName("Testing create semantic version-id for an existing asset name")
  public void testCreateSemanticVersionWhenAssetExists() {
    UUID id = UUID.randomUUID();
    AssetSubJobDto assetSubJobDto = new AssetSubJobDto();
    Map<String, String> metaData = new HashMap<>();
    metaData.put("testKey", "testValue");
    assetSubJobDto.setParentJobId(UUID.randomUUID().toString());
    assetSubJobDto.setFileName(List.of("test.png"));
    assetSubJobDto.setType(AssetCategory.ASSET);
    // assetSubJobDto.setMetaData(metaData);
    assetSubJobDto.setSemanticVersionId(INITIAL_SEMANTIC_VERSION);

    AssetSubJobEntity assetSubJobEntity = assetSubJobMapper.toAssetSubJobEntity(assetSubJobDto);
    assetSubJobEntity.setId(id);
    when(assetSubJobRepository.findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
            any(String.class), any(String.class)))
        .thenReturn(Optional.of(assetSubJobEntity));

    int latestSemanticVersionId =
        assetSubJobService.createSemanticVersionId(assetSubJobDto.getFileName().get(0));
    verify(assetSubJobRepository, times(1))
        .findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
            any(String.class), any(String.class));

    assertEquals(2, latestSemanticVersionId, "Semantic version id is not equal");
  }

  @Test
  @DisplayName("Testing create semantic version-id for a non existing asset name")
  public void testCreateSemanticVersionWhenAssetNotExists() {

    when(assetSubJobRepository.findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
            any(String.class), any(String.class)))
        .thenReturn(Optional.empty());

    int latestSemanticVersionId = assetSubJobService.createSemanticVersionId("test.png");
    verify(assetSubJobRepository, times(1))
        .findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
            any(String.class), any(String.class));

    assertEquals(
        INITIAL_SEMANTIC_VERSION, latestSemanticVersionId, "Semantic version id is not equal");
  }

  @Test
  @Disabled
  @DisplayName("Testing AssetJob Accumulator-OK increment")
  public void testAssetJobAccumulatorOKIncrement() {
    final String jobName = "myJob";
    final UUID parentJobId = UUID.randomUUID();
    final UUID subJobId = UUID.randomUUID();

    when(assetConfigProperties.getMinioEndPoint()).thenReturn("myminioendpoint://");

    final var assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(subJobId);
    assetSubJobEntity.setParentJobId(parentJobId);
    assetSubJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setJobName(jobName);
    when(assetSubJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetSubJobEntity));

    var bucketEvent =
        DataMoverTestUtil.createBucketEvent(
            "f1", jobName, parentJobId.toString(), subJobId.toString());
    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(bucketEvent);

    verify(assetSubJobRepository, times(1)).save(any(AssetSubJobEntity.class));

    var jobIdCaptor = ArgumentCaptor.forClass(UUID.class);
    var keyCaptor = ArgumentCaptor.forClass(String.class);
    var longCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mongoDBOperationsRepository)
        .incrementSubJobsStatusCount(
            jobIdCaptor.capture(), keyCaptor.capture(), longCaptor.capture());

    assertEquals(parentJobId, jobIdCaptor.getValue());
    assertEquals("subJobStatusAccumulator.PROGRESS_OK", keyCaptor.getValue());
    assertEquals(1, longCaptor.getValue());
  }

  @Disabled
  @Test
  @DisplayName("Testing AssetJob Accumulator-OK no increment for duplicated message")
  public void testAssetJobAccumulatorOKNoincrementForDuplication() {
    final String jobName = "myJob";
    final UUID parentJobId = UUID.randomUUID();
    final UUID subJobId = UUID.randomUUID();

    when(assetConfigProperties.getMinioEndPoint()).thenReturn("myminioendpoint://");

    final var assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(subJobId);
    assetSubJobEntity.setParentJobId(parentJobId);
    assetSubJobEntity.setStatus(AssetJobStatus.COMPLETE);
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setJobName(jobName);
    when(assetSubJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetSubJobEntity));

    var bucketEvent =
        DataMoverTestUtil.createBucketEvent(
            "f1", jobName, parentJobId.toString(), subJobId.toString());
    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(bucketEvent);

    verify(assetSubJobRepository, times(1)).save(any(AssetSubJobEntity.class));
    verify(mongoDBOperationsRepository, times(0))
        .incrementSubJobsStatusCount(any(), any(), anyLong());
  }

  @Disabled
  @Test
  @DisplayName("Testing AssetJob Accumulator-Error increment")
  public void testAssetJobAccumulatorErrorIncrement() {
    final String jobName = "myJob";
    final UUID parentJobId = UUID.randomUUID();
    final UUID subJobId = UUID.randomUUID();

    final var assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(subJobId);
    assetSubJobEntity.setParentJobId(parentJobId);
    assetSubJobEntity.setStatus(AssetJobStatus.IN_PROGRESS);
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setJobName(jobName);
    when(assetSubJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetSubJobEntity));

    var bucketEvent =
        DataMoverTestUtil.createBucketEvent(
            "f1", jobName, parentJobId.toString(), subJobId.toString());
    bucketEvent.setError(true);
    bucketEvent.setSubJobId(subJobId.toString());
    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(bucketEvent);

    verify(assetSubJobRepository, times(1)).save(any(AssetSubJobEntity.class));

    var jobIdCaptor = ArgumentCaptor.forClass(UUID.class);
    var keyCaptor = ArgumentCaptor.forClass(String.class);
    var longCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mongoDBOperationsRepository)
        .incrementSubJobsStatusCount(
            jobIdCaptor.capture(), keyCaptor.capture(), longCaptor.capture());

    assertEquals(parentJobId, jobIdCaptor.getValue());
    assertEquals("subJobStatusAccumulator.PROGRESS_FAILED", keyCaptor.getValue());
    assertEquals(1, longCaptor.getValue());
  }

  @Disabled
  @Test
  @DisplayName("Testing AssetJob Accumulator-Error no increment for duplicated message")
  public void testAssetJobAccumulatorErrorNoIncrementForDuplication() {
    final String jobName = "myJob";
    final UUID parentJobId = UUID.randomUUID();
    final UUID subJobId = UUID.randomUUID();

    final var assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(subJobId);
    assetSubJobEntity.setParentJobId(parentJobId);
    assetSubJobEntity.setStatus(AssetJobStatus.ERROR);
    assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
    assetSubJobEntity.setJobName(jobName);
    when(assetSubJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetSubJobEntity));

    var bucketEvent =
        DataMoverTestUtil.createBucketEvent(
            "f1", jobName, parentJobId.toString(), subJobId.toString());
    bucketEvent.setError(true);
    bucketEvent.setSubJobId(subJobId.toString());
    assetSubJobService.updateSubJobStatusAndTriggerUploadAssets(bucketEvent);

    verify(assetSubJobRepository, times(1)).save(any(AssetSubJobEntity.class));
    verify(mongoDBOperationsRepository, times(0))
        .incrementSubJobsStatusCount(any(), any(), anyLong());
  }

  @Test
  @DisplayName("Testing Asset Metadata message creation if Job contains UI part metadata")
  @Disabled
  public void testAssetMetadataMessageProductionForUiPartMetadata() {
    var parentJobId = UUID.randomUUID().toString();
    var assetJobDto = DataMoverTestUtil.createAssetJobDto();
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    assetJobDto.setId(parentJobId);
    assetJobDto.setMetaDataFileName("metadata.json");

    var assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(UUID.randomUUID());

    when(assetSubJobRepository.save(any(AssetSubJobEntity.class))).thenReturn(assetSubJobEntity);
    assetSubJobService.processSubJobs(assetJobDto, userRoles);

    verify(kafkaProducer, timeout(100).times(1)).sendMessage(any(), any(AssetMetadata.class));
  }

  private void buildMockIntermHashCollectionRepoContent(Path metadataPath, UUID parentJobId)
      throws Exception {
    var jsonParser = new JSONParser();
    var object = jsonParser.parse(Files.newBufferedReader(metadataPath));
    final String metaFileName = metadataPath.getFileName().toString();
    final String metaFileHash = (String) ((JSONObject) object).get("meta-file-hash");
    var filesArray = (JSONArray) ((JSONObject) object).get("files");
    for (var fileObj : filesArray) {
      var jsonFileObj = (JSONObject) fileObj;

      var assetMetadata = createNewAssetMetadata(metaFileName, metaFileHash);
      var part = assetMetadata.getFilePartMetadata();
      part.setFileName(List.of((String) jsonFileObj.get("file-name")));
      part.setMetaFileName((String) jsonFileObj.get("meta-file-name"));

      //      lenient()
      //          .when(interimHashCollectionRepository.findById(ArgumentMatchers.eq(assetHashId)))
      //          .thenReturn(Optional.of(hashEntity));
    }
  }

  /*
   The test case below is disabled as it was failing during jenkins build. The reason is under investigation, but it
   has been temporarily disabled as it is of lower priority for now.
   P.S.: It has been re-enabled to test it.
  */
  @Test
  @Disabled
  @DisplayName("Testing processSubJobs where metadata file is present as an asset in the folder.")
  public void testProcessSubJobsOverlappingMetadataFilename() throws Exception {
    var path = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-1");
    var metadataPath = path.resolve("labels_new_format.json");
    final UUID parentJobId = UUID.randomUUID();

    var jobDto = DataMoverTestUtil.createAssetJobDto();
    jobDto.setId(parentJobId.toString());
    jobDto.setAssetLocationType(AssetLocationType.FOLDER);
    jobDto.setLocations(List.of(path.toAbsolutePath().toString()));
    jobDto.setMetaDataFileName(metadataPath.toAbsolutePath().toString());

    var jobEntity = new AssetJobEntity();
    jobEntity.setId(parentJobId);
    jobEntity.setSubJobStatusAccumulator(new HashMap<>());
    jobEntity.getSubJobStatusAccumulator().put(AssetJobStatusAccumulator.TOTAL, -1L);

    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    when(assetJobRepository.findById(ArgumentMatchers.eq(parentJobId)))
        .thenReturn(Optional.of(jobEntity));
    when(assetSubJobRepository.save(any(AssetSubJobEntity.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    buildMockIntermHashCollectionRepoContent(metadataPath, parentJobId);

    assetSubJobService.processSubJobs(jobDto, userRoles);

    jobExecutorService.shutdown();
    jobExecutorService.awaitTermination(5, TimeUnit.SECONDS);

    // total update
    var jobEntityCaptor = ArgumentCaptor.forClass(AssetJobEntity.class);
    verify(assetJobRepository, times(1)).save(jobEntityCaptor.capture());
    assertEquals(parentJobId, jobEntityCaptor.getValue().getId());
    assertEquals(
        4,
        jobEntityCaptor
            .getValue()
            .getSubJobStatusAccumulator()
            .get(AssetJobStatusAccumulator.TOTAL));

    var subJobEntityCaptor = ArgumentCaptor.forClass(AssetSubJobEntity.class);
    verify(assetSubJobRepository, times(4)).save(subJobEntityCaptor.capture());

    //    var hashEntityCaptor = ArgumentCaptor.forClass(InterimHashCollectionEntity.class);
    //    verify(interimHashCollectionRepository, times(3)).save(hashEntityCaptor.capture());

    var assetMetadataCaptor = ArgumentCaptor.forClass(AssetMetadata.class);
    verify(kafkaProducer, times(3)).sendMessage(Mockito.any(), assetMetadataCaptor.capture());
    var allValues = assetMetadataCaptor.getAllValues();
    assertAssetMetadata(
        allValues.get(0),
        "labels_new_format.json",
        "7c98d48dc62619fd43a754c1d4af15901b2f8a277d7e2f92353186185884ddaa");
    assertAssetMetadata(
        allValues.get(1),
        "labels_new_format.json",
        "7c98d48dc62619fd43a754c1d4af15901b2f8a277d7e2f92353186185884ddaa");
    var list = new ArrayList<AssetMetadata>();
    list.add(null);
    list.add(null);
    list.add(null);
    allValues.forEach(
        assetMetadataVal -> {
          int id =
              Character.getNumericValue(
                  assetMetadataVal.getFilePartMetadata().getMetaFileName().charAt(6));
          System.out.println(id);
          list.set(id - 1, assetMetadataVal);
        });
    list.forEach(
        list1 -> {
          System.out.println(list1.getFilePartMetadata().getFileName());
        });
    assertFilePartMetadata(
        list.get(2).getFilePartMetadata(),
        "labels3.json",
        "8effc4ffae64cb0c7a140969038e8889626e39ebfb8454c5887dcd14dfcee281",
        "labels.json",
        "{\"label\":\"sunflowers\"}");
    assertFilePartMetadata(
        list.get(0).getFilePartMetadata(),
        "labels1.json",
        "4e3f308ae97aaeb2442693aad82d9b5b8a76ae87b7667ea1e68f609d24ba928c",
        "fake-image-1.jpg",
        "{\"label\":\"sunflowers\"}");
  }

  private static void assertAssetMetadata(
      AssetMetadata am, String metaFileName, String metaFileHash) {
    assertEquals(am.getMetaFileName(), metaFileName);
  }

  private static void assertFilePartMetadata(
      File assertFile, String metaFileName, String fileHash, String fileName, String md) {
    assertEquals(assertFile.getMetaFileName(), metaFileName);
    assertEquals(assertFile.getFileName(), fileName);
    // assertEquals(assertFile.getMetadata(), md);
  }

  private AssetMetadata createNewAssetMetadata(String metaFileName, String metaFileHash) {
    var file = new File();
    file.setMetaFileName("");
    file.setFileName(List.of("file1.jpg"));
    file.setOwner("");
    var assetMetadata = new AssetMetadata();
    assetMetadata.setFilePartMetadata(file);
    assetMetadata.setMetaFileName(metaFileName);
    assetMetadata.setParameters("");
    assetMetadata.setUiPartMetadata(Collections.emptyMap());
    assetMetadata.setContentAssetMetadata("{\"label\":\"sunflowers\"}");

    return assetMetadata;
  }

  @Test
  @DisplayName("Testing update bulk metadata")
  public void testUpdateBulkMetadata() {
    final RetrieveAssetJobDto retrieveAssetJobDto = DataMoverTestUtil.createRetrieveAssetJobDto();
    final AssetJobDto assetJobEntity = DataMoverTestUtil.createAssetJobDto();
    final AssetSubJobEntity assetSubJobEntity = new AssetSubJobEntity();
    assetSubJobEntity.setId(UUID.randomUUID());
    final UpdateBulkMetadataRequestDto updateBulkMetadataRequestDto =
        DataMoverTestUtil.createUpdateBulkMetadataRequestDto();
    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, 5L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 0L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);
    assetJobEntity.setSubJobStatusAccumulator(subJobStatusAccumulator);
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");
    when(assetJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetJobMapper.toJobEntity(assetJobEntity)));
    when(assetSubJobRepository.countByParentJobId(any(UUID.class))).thenReturn(4L);
    assetSubJobService.updateBulkMetadata(updateBulkMetadataRequestDto, userRoles);
    verify(assetJobRepository, times(1)).findById(any(UUID.class));
    verify(assetSubJobRepository, times(1)).countByParentJobId(any(UUID.class));
  }

  @Test
  @DisplayName("Testing update bulk metadata with exception")
  public void testUpdateBulkMetadataWithException() {
    final AssetJobDto assetJobEntity = DataMoverTestUtil.createAssetJobDto();
    final UpdateBulkMetadataRequestDto updateBulkMetadataRequestDto =
        DataMoverTestUtil.createUpdateBulkMetadataRequestDto();
    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    when(assetJobRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(assetJobMapper.toJobEntity(assetJobEntity)));
    when(assetSubJobRepository.countByParentJobId(any(UUID.class))).thenReturn(5L);
    DataMoverException thrown =
        Assertions.assertThrows(
            DataMoverException.class,
            () -> {
              assetSubJobService.updateBulkMetadata(updateBulkMetadataRequestDto, userRoles);
            });
    assertTrue(thrown.getMessage().contains("has no space for new asset metadata count"));
  }

  private void modifyMetatadaAddSelfReference(Path metadataPath) throws Exception {
    var jsonParser = new JSONParser();
    var object = jsonParser.parse(Files.newBufferedReader(metadataPath));
    var filesArray = (JSONArray) ((JSONObject) object).get("files");
    String name = metadataPath.getFileName().toString();
    var newFile = new JSONObject();
    newFile.put("file-hash", "8effc4ffae64cb0c7a140969038e8889626e39ebfb8454c5887dcd14dfcee281");
    newFile.put("file-name", name);
    newFile.put("meta-file-name", name);
    newFile.put("owner", "test1");

    filesArray.add(newFile);

    Files.write(metadataPath, ((JSONObject) object).toJSONString().getBytes());
  }

  @Test
  @DisplayName("Testing Metadata containing a reference to itself. Action: ignore that entry")
  @Disabled
  public void testMetadataRefersToItself(@TempDir Path tempDir) throws Exception {
    var srcPath = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-1");
    var dataPath = tempDir.resolve("tmp");
    DataMoverTestUtil.copyFiles(srcPath, dataPath);
    var metadataPath = dataPath.resolve("labels_new_format.json");
    modifyMetatadaAddSelfReference(metadataPath);

    final UUID parentJobId = UUID.randomUUID();

    var jobDto = DataMoverTestUtil.createAssetJobDto();
    jobDto.setId(parentJobId.toString());
    jobDto.setAssetLocationType(AssetLocationType.FOLDER);
    jobDto.setLocations(List.of(dataPath.toAbsolutePath().toString()));
    jobDto.setMetaDataFileName(metadataPath.toAbsolutePath().toString());

    var jobEntity = new AssetJobEntity();
    jobEntity.setId(parentJobId);
    jobEntity.setSubJobStatusAccumulator(new HashMap<>());
    jobEntity.getSubJobStatusAccumulator().put(AssetJobStatusAccumulator.TOTAL, -1L);

    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    when(assetJobRepository.findById(ArgumentMatchers.eq(parentJobId)))
        .thenReturn(Optional.of(jobEntity));
    when(assetSubJobRepository.save(any(AssetSubJobEntity.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    buildMockIntermHashCollectionRepoContent(metadataPath, parentJobId);

    assetSubJobService.processSubJobs(jobDto, userRoles);

    jobExecutorService.shutdown();
    jobExecutorService.awaitTermination(5, TimeUnit.SECONDS);

    // it ignored the self reference
    verify(assetSubJobRepository, times(4)).save(any(AssetSubJobEntity.class));
  }

  @Test
  @DisplayName("Testing importing filenames with ignore list present. Without metadata file.")
  @Disabled
  public void testIgnoreFilenamesWithoutMetadataFile(@TempDir Path tempDir) throws Exception {
    var srcPath = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-1");
    var dataPath = tempDir.resolve("tmp");
    DataMoverTestUtil.copyFiles(srcPath, dataPath);
    Files.createFile(dataPath.resolve("Thumbs.db"));
    Files.createFile(dataPath.resolve(".aaa"));
    Files.createFile(dataPath.resolve(".bbb"));

    final UUID parentJobId = UUID.randomUUID();

    var jobDto = DataMoverTestUtil.createAssetJobDto();
    jobDto.setId(parentJobId.toString());
    jobDto.setAssetLocationType(AssetLocationType.FOLDER);
    jobDto.setLocations(List.of(dataPath.toAbsolutePath().toString()));

    var jobEntity = new AssetJobEntity();
    jobEntity.setId(parentJobId);
    jobEntity.setSubJobStatusAccumulator(new HashMap<>());
    jobEntity.getSubJobStatusAccumulator().put(AssetJobStatusAccumulator.TOTAL, -1L);

    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    when(assetJobRepository.findById(ArgumentMatchers.eq(parentJobId)))
        .thenReturn(Optional.of(jobEntity));
    when(assetSubJobRepository.save(any(AssetSubJobEntity.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    // buildMockIntermHashCollectionRepoContent(metadataPath, parentJobId);

    when(filenameFilterConfig.getIgnoreListRegex())
        .thenReturn(List.of("(?i)thumbs\\.db", "^\\..*"));
    ReflectionTestUtils.invokeMethod(assetSubJobService, "postConstruct");

    assetSubJobService.processSubJobs(jobDto, userRoles);

    jobExecutorService.shutdown();
    jobExecutorService.awaitTermination(5, TimeUnit.SECONDS);

    // it ignored the self reference
    verify(assetSubJobRepository, times(4)).save(any(AssetSubJobEntity.class));
  }

  @Test
  @DisplayName("Testing importing filenames with ignore list present. With metadata file.")
  @Disabled
  public void testIgnoreFilenamesWithMetadataFile(@TempDir Path tempDir) throws Exception {
    var srcPath = DataMoverTestUtil.getFileFromClasspath("assets-with-metadata-1");
    var dataPath = tempDir.resolve("tmp");
    DataMoverTestUtil.copyFiles(srcPath, dataPath);
    Files.createFile(dataPath.resolve("Thumbs.db"));
    Files.createFile(dataPath.resolve(".aaa"));
    Files.createFile(dataPath.resolve(".bbb"));
    var metadataPath = dataPath.resolve("labels_new_format.json");

    final UUID parentJobId = UUID.randomUUID();

    var jobDto = DataMoverTestUtil.createAssetJobDto();
    jobDto.setId(parentJobId.toString());
    jobDto.setAssetLocationType(AssetLocationType.FOLDER);
    jobDto.setLocations(List.of(dataPath.toAbsolutePath().toString()));
    jobDto.setMetaDataFileName(metadataPath.toAbsolutePath().toString());

    var jobEntity = new AssetJobEntity();
    jobEntity.setId(parentJobId);
    jobEntity.setSubJobStatusAccumulator(new HashMap<>());
    jobEntity.getSubJobStatusAccumulator().put(AssetJobStatusAccumulator.TOTAL, -1L);

    var userRoles = List.of("CollManagerPrimaryAutomotiveRole");

    when(assetJobRepository.findById(ArgumentMatchers.eq(parentJobId)))
        .thenReturn(Optional.of(jobEntity));
    when(assetSubJobRepository.save(any(AssetSubJobEntity.class)))
        .thenAnswer(i -> i.getArguments()[0]);

    buildMockIntermHashCollectionRepoContent(metadataPath, parentJobId);

    when(filenameFilterConfig.getIgnoreListRegex())
        .thenReturn(List.of("(?i)thumbs\\.db", "^\\..*"));
    ReflectionTestUtils.invokeMethod(assetSubJobService, "postConstruct");

    assetSubJobService.processSubJobs(jobDto, userRoles);

    jobExecutorService.shutdown();
    jobExecutorService.awaitTermination(5, TimeUnit.SECONDS);

    // it ignored the self reference
    verify(assetSubJobRepository, times(4)).save(any(AssetSubJobEntity.class));
  }

  @Test
  @DisplayName("Testing testUpdateAndGetPrevStatus()")
  public void testUpdateAndGetPrevStatus() {

    var assetSubJobDto = DataMoverTestUtil.createAssetSubJobDto();
    var assetSubJobEntity = assetSubJobMapper.toAssetSubJobEntity(assetSubJobDto);
    assetSubJobDto.setIndexed(true);
    assetSubJobDto.setStored(true);

    var assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setId(UUID.randomUUID().toString());
    var assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);

    when(mongoDBOperationsRepository.updateSubJobAndStatus(any(AssetSubJobDto.class)))
        .thenReturn(assetSubJobEntity);
    when(mongoDBOperationsRepository.incrementSubJobsStatusCount(
            any(UUID.class), any(String.class), any(Long.class)))
        .thenReturn(assetJobEntity);

    assetSubJobService.updateAndGetPrevStatus(assetSubJobDto);

    verify(mongoDBOperationsRepository, times(1)).updateSubJobAndStatus(any(AssetSubJobDto.class));
    verify(mongoDBOperationsRepository, times(1))
        .incrementSubJobsStatusCount(any(UUID.class), any(String.class), any(Long.class));
  }

  @Test
  @DisplayName("Testing testUpdateAssetStatus()")
  void testUpdateAssetStatus() {
    var assetJobDto = DataMoverTestUtil.createAssetJobDto();
    assetJobDto.setId(UUID.randomUUID().toString());

    var subJobStatusAccumulator = new HashMap<AssetJobStatusAccumulator, Long>();
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.TOTAL, 3L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_OK, 3L);
    subJobStatusAccumulator.put(AssetJobStatusAccumulator.PROGRESS_FAILED, 0L);

    assetJobDto.setSubJobStatusAccumulator(subJobStatusAccumulator);
    var assetJobEntity = assetJobMapper.toJobEntity(assetJobDto);

    assetSubJobService.updateAssetJobStatus(assetJobEntity);

    verify(mongoDBOperationsRepository, times(1))
        .updateAssetJobStatus(any(UUID.class), any(String.class));
  }
}
