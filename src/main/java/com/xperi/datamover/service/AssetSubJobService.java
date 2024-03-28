package com.xperi.datamover.service;

import com.xperi.datamover.config.AssetConfigProperties;
import com.xperi.datamover.config.DataMoverConfigProperties;
import com.xperi.datamover.config.FilenameFilterConfig;
import com.xperi.datamover.constants.AssetCategory;
import com.xperi.datamover.constants.AssetJobStatus;
import com.xperi.datamover.constants.AssetJobStatusAccumulator;
import com.xperi.datamover.dto.*;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.exception.DataMoverException;
import com.xperi.datamover.model.AssetEvent;
import com.xperi.datamover.model.minio.BucketEvent;
import com.xperi.datamover.repository.AssetJobRepository;
import com.xperi.datamover.repository.AssetSubJobRepository;
import com.xperi.datamover.repository.MongoDBOperationsRepository;
import com.xperi.datamover.task.AssetTask;
import com.xperi.datamover.task.JobExecutorService;
import com.xperi.datamover.task.MetadataTask;
import com.xperi.datamover.util.*;
import com.xperi.schema.accumulator.AccumulatorEvent;
import com.xperi.schema.metadata.AssetMetadata;
import com.xperi.schema.metadata.Operations;
import com.xperi.schema.subjobevent.AssetSubJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class contains all the sub job related operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSubJobService {

    private static final int INITIAL_SEMANTIC_VERSION = 1;
    private final AssetSubJobRepository assetSubJobRepository;
    private final AssetJobRepository assetJobRepository;
    private final AssetSubJobMapper assetSubJobMapper;
    private final AssetJobMapper assetJobMapper;
    private final KafkaProducer kafkaProducer;
    private final DataMoverConfigProperties dataMoverConfigProperties;
    private final AssetConfigProperties assetConfigProperties;
    private final KafkaUtil kafkaUtil;
    private final MongoTemplate mongoTemplate;
    private final JobExecutorService subJobExecutorService;
    private final MongoDBOperationsRepository mongoDBOperationsRepository;
    private final FilenameFilterConfig filenameFilterConfig;

    private List<Pattern> filenameFilterPatterns = Collections.emptyList();

    private static AssetSubJobDto createAssetSubJobDtoForAsset(AssetJobDto assetJobDto, List<String> fileNames) {
        var dto = new AssetSubJobDto();
        dto.setStatus(AssetJobStatus.IN_PROGRESS);
        dto.setParentJobId(assetJobDto.getId());
        dto.setFilePath(getAssetPathFromFileNames(fileNames,assetJobDto.getLocations().get(0)));
        dto.setType(AssetCategory.ASSET);
        dto.setFileName(fileNames);
        dto.setJobName(assetJobDto.getJobName());
        return dto;
    }

    private static List<String> getAssetPathFromFileNames(List<String> fileNames, String fileLocation){
        if(fileNames.size() == 1){
            return List.of(Paths.get(fileLocation, fileNames.get(0)).toString());
        } else if(fileNames.size() > 1) {
            List<String> filePaths = new ArrayList<>();
            for (int i = 0; i<fileNames.size(); i++) {
                filePaths.add(Paths.get(fileLocation, fileNames.get(i)).toString());
            }
            return filePaths;
        } else{
            throw new DataMoverException("file-name array is empty");
        }
    }

    @PostConstruct
    private void postConstruct() {
        setupFilenameFilters();
    }

    private void setupFilenameFilters() {
        var list = filenameFilterConfig.getIgnoreListRegex();
        if (!CollectionUtils.isEmpty(list)) {
            filenameFilterPatterns = list.stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toList());
        }
        log.info("filename-filter.ignore-list={}", filenameFilterPatterns);
    }

    /**
     * This method creates a sub job & saves in database
     *
     * @param assetSubJobDto job details to create
     * @return It returns sub-job id
     */
    @SneakyThrows
    public String create(AssetSubJobDto assetSubJobDto) {
        Assert.notNull(assetSubJobDto, "Sub job details are empty");
        final var assetSubJobEntity =
                assetSubJobMapper.toAssetSubJobEntity(assetSubJobDto);
        assetSubJobEntity.setId(UUID.randomUUID());
        assetSubJobEntity.setCreatedAt(Calendar.getInstance().getTime());
        log.debug("Sub Job details before creating entry in database : {}", assetSubJobEntity);
        return assetSubJobRepository.save(assetSubJobEntity).getId().toString();
    }

    /**
     * Processing new sub jobs
     *
     * @param assetJobDto sub-job details
     */
    @Async("jobExecutorService")
    public void processSubJobs(AssetJobDto assetJobDto, List<String> userRoles) {
        Assert.notNull(assetJobDto, "Job details are empty");

        // If metadata file exists, validate and parse the metadata file
        if (StringUtils.isNotEmpty(assetJobDto.getMetaDataFileName())) {
            processMetadataAndAssetFiles(assetJobDto,userRoles);
        } else {
            log.error("Metadata filename is not present in the request. JobId - {}",assetJobDto.getId());
            throw new DataMoverException("Metadata filename is not present in the request. JobId - %s".formatted(assetJobDto.getId()));
        }
    }

    /**
     * find all existing sub jobs by parent job id
     *
     * @return list of asset sub-jobs
     */
    public List<AssetSubJobDto> findByParentJobId(String parentJobId) {
        Assert.hasText(parentJobId, "Parent job id is empty");
        UUID jobId = UUID.fromString(parentJobId);
        Optional<AssetJobEntity> parentJobFound = assetJobRepository.findById(jobId);
        if (parentJobFound.isPresent()) {
            final List<AssetSubJobEntity> assetSubJobEntities =
                    assetSubJobRepository.findByParentJobId(jobId);
            log.debug("Found {} child assets", assetSubJobEntities.size());
            return assetSubJobMapper.toAssetSubJobDtoList(assetSubJobEntities);
        } else {
            log.error("Failed to find the job in database. JodId - {}", jobId);
            throw new DataMoverException("Failed to find the job in database. JodId - %s".formatted(jobId));
        }
    }

    /**
     * To find all existing Pageable sub-jobs by parent job id
     *
     * @param parentJobId id to search
     * @param p           page
     * @return result
     */
    public Page<AssetSubJobDto> findByParentJobIdPageable(String parentJobId, Pageable p) {
        Assert.hasText(parentJobId, "Parent job id is empty");
        UUID jobId = UUID.fromString(parentJobId);
        Optional<AssetJobEntity> parentJobFound = assetJobRepository.findById(jobId);
        if (parentJobFound.isPresent()) {
            final Page<AssetSubJobEntity> assetSubJobEntities =
                    assetSubJobRepository.findByParentJobId(jobId, p);
            if (log.isDebugEnabled()) {
                log.debug("Found {} child assets", assetSubJobEntities.getContent().size());
            }
            return assetSubJobEntities.map(assetSubJobMapper::toAssetSubJobDto);
        } else {
            log.error("Failed to find the job in database. JobId {}", jobId);
            throw new DataMoverException("Failed to find the job in database. JobId : %s".formatted(jobId));
        }
    }


    /**
     * To find all existing Pageable sub-jobs by parent job id and Job Status
     *
     * @param parentJobId id to search
     * @param status      status
     * @param p           page
     * @return result
     */
    public Page<AssetSubJobDto> findByParentJobIdAndStatusPageable(String parentJobId,String status, Pageable p) {
        Assert.hasText(parentJobId, "Parent job id is empty");
        UUID jobId = UUID.fromString(parentJobId);
        if(checkValidStatus(status)) {
            Optional<AssetJobEntity> parentJobFound = assetJobRepository.findById(jobId);
            if (parentJobFound.isPresent()) {
                final Page<AssetSubJobEntity> assetSubJobEntities =
                        assetSubJobRepository.findByParentJobIdAndStatus(jobId,AssetJobStatus.valueOf(status), p);
                    log.debug("Found {} child assets", assetSubJobEntities.getContent().size());
                return assetSubJobEntities.map(assetSubJobMapper::toAssetSubJobDto);
            } else {
                log.error("Failed to find the job in database. JobId {}", jobId);
                throw new DataMoverException("Failed to find the job in database. JobId : %s".formatted(jobId));
            }
        } else {
            throw new DataMoverException("Please provide a valid job status. e.g. COMPLETE, IN_PROGRESS, ERROR ");
        }
    }

    /**
     *
     *
     * @param status
     * @return
     */
    private boolean checkValidStatus(String status){
        try{
            AssetJobStatus assetJobStatus = AssetJobStatus.valueOf(status);
            return true;
        }catch (Exception e){
           return false;
        }
    }


    /**
     * To send asset sub job message to Kafka
     *
     * @param assetSubJobEvent
     */
    @SneakyThrows
    public void sendAssetSubJobMessageToKafka(AssetSubJobEvent assetSubJobEvent) {
        // Send message to kafka for sub jobs
        kafkaProducer.sendMessage(dataMoverConfigProperties.getMinioSubJobTopic(), assetSubJobEvent);
    }

    /**
     * Processing Metadata file
     *
     * @param assetJobDto the job holding the metadata info
     */
    public void processMetadataFile(AssetJobDto assetJobDto, List<String> userRoles) {
        final var assetSubJobDto = new AssetSubJobDto();
        final var metaDataFilePath = Paths.get(assetJobDto.getMetaDataFileName());
        assetSubJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
        assetSubJobDto.setParentJobId(assetJobDto.getId());
        assetSubJobDto.setFilePath(List.of(metaDataFilePath.toString()));
        assetSubJobDto.setType(AssetCategory.METADATA);
        assetSubJobDto.setFileName(List.of(metaDataFilePath.getFileName().toString()));
        assetSubJobDto.setJobName(assetJobDto.getJobName());

        log.info("Started processing metadata file - {} for jobID - {}",assetJobDto.getMetaDataFileName(), assetJobDto.getId());

        // Send message to ml-asset for uploading metadata file into minIO
        var assetTask = new AssetTask(this, assetSubJobDto, assetJobDto.getMetaDataFileName(),
                null, null, userRoles,null);
        submitForAsyncExecution(assetTask);
    }

    /**
     * To submit a Asynchronous task for execution
     *
     * @param assetTask
     */
    private void submitForAsyncExecution(AssetTask assetTask) {
        try {
            subJobExecutorService.execute(assetTask);
            log.debug("Submitted task for AssetSubJobId - {}", assetTask.getAssetSubJobId());
        } catch (final Exception e) {
            log.error("Failed to submit task for AssetSubJobId - {}", assetTask.getAssetSubJobId(), e);
        }
    }

    private void submitForAsyncMetadataExecution(MetadataTask metadataTask) {
        try {
            subJobExecutorService.execute(metadataTask);
            log.debug("Submitted task for AssetSubJob.id={}", metadataTask.getAssetSubJobId());
        } catch (final Exception e) {
            log.error("Failed to submit task for AssetSubJobId - {}", metadataTask.getAssetSubJobId(), e);
        }
    }

    /**
     * This method is to exclude the provided metadata file from all the asset list,
     * Because Asset files and metadata files are in same folder
     *
     * @param assetsList
     * @param metadataAbsFileName
     * @return
     */
    private List<String> filterAssetsOfMetadataFilename(List<String> assetsList, String metadataAbsFileName) {
        if (StringUtils.isNotEmpty(metadataAbsFileName)) {
            var metadataFileName = new File(metadataAbsFileName).getName();
            int pos = assetsList.indexOf(metadataFileName);
            if (pos >= 0) {
                var list1 = assetsList.subList(0, pos);
                var list2 = assetsList.subList(pos + 1, assetsList.size());
                assetsList = new ArrayList<>();
                assetsList.addAll(list1);
                assetsList.addAll(list2);
            }
        }
        return assetsList;
    }

    /**
     * To filter few hidden file extensions in the assets list. this files are not part of the actual Asset file set
     *
     * @param assetsList
     * @return
     */
    private List<String> filterAssetsOfIgnoredFiles(List<String> assetsList) {
        if (!filenameFilterPatterns.isEmpty() && assetsList.stream().anyMatch(this::isIgnoreFilename)) {
                assetsList = new ArrayList<>(assetsList);
                assetsList.removeIf(this::isIgnoreFilename);
        }
        return assetsList;
    }

    /**
     * To get the list of all the files inside a folder path. The listing is not recursive.
     *
     * @param assetJobDto
     * @return
     */
    private List<String> getAssetList(AssetJobDto assetJobDto) {
        var locationType = assetJobDto.getAssetLocationType();
        return switch (locationType) {
            case FOLDER -> AssetJobUtil.listFilesFromDir(Paths.get(assetJobDto.getLocations().get(0)));
            case FILE -> assetJobDto.getLocations();
            default -> {
                log.error("Unknown asset location type : {}", locationType);
                throw new DataMoverException("Unknown asset location type");
            }
        };
    }

    /**
     * To prepare the exact asset file list of a particular job
     *
     * @param assetJobDto
     * @return
     */
    private List<String> getPreparedAssets(AssetJobDto assetJobDto) {
        var assetsList = getAssetList(assetJobDto);
        assetsList = filterAssetsOfMetadataFilename(assetsList, assetJobDto.getMetaDataFileName());

        int size = assetsList.size();
        assetsList = filterAssetsOfIgnoredFiles(assetsList);
        if (size != assetsList.size()) {
            log.warn("jobId={}, ignored {}/{} assets", assetJobDto.getId(), size - assetsList.size(), size);
        }
        return assetsList;
    }


    public void processMetadataAndAssetFiles(AssetJobDto assetJobDto, List<String> userRoles) {
        Assert.notNull(assetJobDto, "Job details are empty");
        Assert.notEmpty(userRoles, "userRoles list should not be empty");

        AtomicInteger totalProcessedFileCount = new AtomicInteger();
        // Get the prepared asset list
        var assetsList = getPreparedAssets(assetJobDto);
        log.info("Total asset files we are going to process for jobID - {} is {}", assetJobDto.getId(),assetsList.stream().count());
        // To check if the asset list is empty or not
        if (!CollectionUtils.isEmpty(assetsList)) {
            if (StringUtils.isNotEmpty(assetJobDto.getMetaDataFileName())) {
                // to create a sub-job for metadata and to send a message to ml-asset to upload the metadata file into minIO
                processMetadataFile(assetJobDto, userRoles);
                totalProcessedFileCount.getAndIncrement();
            }
            // To parse the metadata file

            boolean fileReadFailure = false;
            try {

                // To get UI-part metadata
                final Map<String, String> map = assetJobDto.getMetaData() == null ? Collections.emptyMap() : assetJobDto.getMetaData();
                // To get Path of the metadata file
                final var file = Paths.get(assetJobDto.getMetaDataFileName());
                // This the metadata file name from where we are going to read the metadata of each asset file
                final String metadataFilename = file.getFileName().toString();
                // Now we are going to read the root level keys of the metadata file like 'meta-file-name', 'parameters' and 'files'
                final var header = MetadataJsonParserUtil.readHeader(file);
                log.info("jobId = {}, Started processing Asset files from metadata file - {}", assetJobDto.getId(), file);


                AtomicInteger finalTotalProcessedFileCount = totalProcessedFileCount;
                // This is to read each file's metadata content from the 'files' array in metadata.json
                MetadataJsonParserUtil.readFileParts(file, assetFile -> {
                    try {
                        if (assetFile.getFile_name().size() == 1 &&
                                metadataFilename.equals(assetFile.getFile_name().get(0))) {
                            log.warn("jobId={}, metadata file {} contains FilePart reference to itself. Entry ignored.",
                                    assetJobDto.getId(), file);
                            return true; //true all good, continue parsing. return false to stop parsing.
                        }

                        var assetMetadata = new AssetMetadata();
                        assetMetadata.setOperation(Operations.CREATE);// CREATE Asset Operation
                        assetMetadata.setMetaFileName(header.getMetaFileName());// metadata filename
                        assetMetadata.setParameters(header.getParameters().toString());// parameters
                        assetMetadata.setUiPartMetadata(map); // UI-part metadata
                        assetMetadata.setContentAssetMetadata(assetFile.getMetadata());// the metadata key content

                        com.xperi.schema.metadata.File fileFromSchema = assetJobMapper.toFileSchema(assetFile);
                        assetMetadata.setFilePartMetadata(fileFromSchema);// To save the filePart metadata

//                      if ((!assetFile.getFile_name().isEmpty())
//                                      && assetsList.contains(String.join(",", assetFile.getFile_name()))) {

                            List<String> fileNames = assetFile.getFile_name();
                            var assetSubJobDto = createAssetSubJobDtoForAsset(assetJobDto, fileNames);
                            submitForAsyncExecution(
                                    new AssetTask(this, assetSubJobDto, assetJobDto.getMetaDataFileName(),
                                            null, assetJobDto.getMetaData(), userRoles,assetMetadata));
                            return true; //true all good, continue parsing. return false to stop parsing.

//                        } else {
//                            log.error("Asset file details found in metadata file, but not found in Landing zone asset list. AssetName -  {}",assetFile);
//                            updateJobStatusToError(assetJobDto,
//                                    "Asset File details found in metadata file, but not found in Landing zone asset list. AssetName - " + assetFile);
//                            finalTotalProcessedFileCount.getAndIncrement();
//                            return false;//true all good, continue parsing. return false to stop parsing.
//                        }

                    } catch (AvroRuntimeException e) {
                        updateJobStatusToError(assetJobDto,
                                e.getMessage() + "for JobId - %s , AssetFileName - %s".formatted(assetJobDto.getId(),assetFile));
                        log.error("Avro Run Time Exception occurred while processing Assets files jobId - {}",assetJobDto.getId(), e);
                        return false;//true all good, continue parsing. return false to stop parsing.
                    } catch (Exception e){
                        updateJobStatusToError(assetJobDto,
                                e.getMessage() + "for JobId - %s , AssetFileName - %s".formatted(assetJobDto.getId(),assetFile));

                        log.error("Exception occurred while processing Assets files. jobId - {} ",assetJobDto.getId(), e);
                        return false;//true all good, continue parsing. return false to stop parsing.
                    } finally {
                        finalTotalProcessedFileCount.getAndIncrement();
                    }
                });
                updatingTotalSubJobsCount(assetJobDto, finalTotalProcessedFileCount.get());
                log.info("Finished processing metadata file for jobId ={} from metadata file - {} ", assetJobDto.getId(),file);

            } catch (IOException e) {
                log.error("Error Occurred while processing Metadata and Asset file. JobId : {}",assetJobDto.getId(), e);
                updateJobStatusToError(assetJobDto,
                        "Error Occurred while processing Metadata and Asset file. Unable to read metadata file. JobId: " + assetJobDto.getMetaDataFileName());
                fileReadFailure = true;
            } catch (Exception e){
                log.error("Error Occurred while processing Metadata and Asset file. JobId : {}",assetJobDto.getId(), e);
                updateJobStatusToError(assetJobDto,
                        "Error Occurred while processing Metadata and Asset file. Unable to read metadata file. JobId: " + assetJobDto.getMetaDataFileName());
            } finally {
                log.info("TOTAL Files Processed - {} , for JobId : {}", totalProcessedFileCount.get(), assetJobDto.getId());
                if (totalProcessedFileCount.get() == 0) {
                    throw new DataMoverException("No asset file got processed. There must be some issue. JobId - " + assetJobDto.getId());
                }
                if (fileReadFailure) {
                    throw new DataMoverException("Error Occurred while processing Metadata and Asset file. Unable to read metadata file. JobId: " + assetJobDto.getId());
                }
            }
        } else {
            String msg = String.format("Assets list is empty for Asset Job Id (%s)", assetJobDto.getId());
            updateJobStatusToError(assetJobDto, msg);
            log.error(msg);
            throw new DataMoverException(msg);
        }
    }


    /**
     * @param jobId job id
     * @return the status of sub jobs with their count
     */
    public Map<AssetJobStatus, Long> countBasedOnStatus(String jobId) {
        return assetSubJobRepository.countBasedOnStatus(jobId, mongoTemplate);
    }

    /**
     * This method finds asset subJobs with given job status
     *
     * @param dto AssetSubJobRequestDto
     * @return AssetSubJobResponseDto
     */
    public AssetSubJobResponseDto findAssetSubJobs(AssetSubJobRequestDto dto) {
        Assert.notNull(dto, "Retrieve detail is null");

        int sizeOfPage = dataMoverConfigProperties.getSizeOfPage();
        int pageNo = dto.getPageNo();

        Pageable paging = PageRequest.of(pageNo, sizeOfPage);

        Page<AssetSubJobEntity> entityPage =
                assetSubJobRepository.findByStatusAndType(dto.getStatus(), AssetCategory.ASSET, paging);

        var assetSubJobList = assetSubJobMapper.toAssetSubJobDtoList(entityPage.getContent());

        var responseDto = new AssetSubJobResponseDto();
        responseDto.setTotalPages(entityPage.getTotalPages());
        responseDto.setCurrentPage(entityPage.getNumber());
        responseDto.setTotalItems(entityPage.getTotalElements());
        responseDto.setAssetList(assetSubJobList);

        return responseDto;
    }

    public AssetJobStatus updateAndGetPrevStatus(AssetSubJobDto assetSubJobDto) {
        Assert.notNull(assetSubJobDto, "Update details for sub job are empty");
        Assert.hasText(assetSubJobDto.getId(), "Asset sub job ID is missing");
        var assetSubJobBeforeUpdate = new AssetSubJobEntity();
        try {
            assetSubJobBeforeUpdate = mongoDBOperationsRepository.updateSubJobAndStatus(assetSubJobDto);

            // To check the status for stored and indexed field
            var indexed = assetSubJobDto.isIndexed() || assetSubJobBeforeUpdate.isIndexed();
            var stored = assetSubJobDto.isStored() || assetSubJobBeforeUpdate.isStored();

            //assetSubJobBeforeUpdate.getStatus() == AssetJobStatus.IN_PROGRESS is required to prevent
            //reading the same message from Kafka twice and incrementing the value in accumulator
            if (indexed && stored
                    && assetSubJobBeforeUpdate.getStatus() == AssetJobStatus.IN_PROGRESS) {
                var assetJobEntityAfterInc = updateSubJobStatusAccumulatorForProgressOK(assetSubJobBeforeUpdate.getParentJobId());
                updateAssetJobStatus(assetJobEntityAfterInc);
            }
        } catch (Exception e) {
            log.error("Exception occurred while updating sub job : {}", assetSubJobDto, e);
        }
        return assetSubJobBeforeUpdate.getStatus();
    }

    public void updateAssetJobStatus(AssetJobEntity assetJobEntity) {
        try {
            var progressOk = assetJobEntity.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_OK);
            var progressFailed = assetJobEntity.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.PROGRESS_FAILED);
            var total = assetJobEntity.getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL);
            var totalOfProgressOkAndProgressFailed = progressOk + progressFailed;

            if (total <= totalOfProgressOkAndProgressFailed) {
                var status = AssetJobStatus.COMPLETE.name();
                if (total < totalOfProgressOkAndProgressFailed) {
                    status = AssetJobStatus.ERROR.name();
                    log.error("One or more subJobs were processed more than once with parentJobId {}", assetJobEntity.getId());
                } else if (progressFailed > 0) {
                    status = AssetJobStatus.ERROR.name();
                }
                mongoDBOperationsRepository.updateAssetJobStatus(assetJobEntity.getId(), status);
            }
        } catch (Exception ex) {
            throw new DataMoverException("Error updating Asset with id %s".formatted(assetJobEntity.getId()), ex);
        }
    }

    private void updateHistory(AssetSubJobEntity assetSubJobEntityFound, Date timeNow) {
        var assetJobHistory = new AssetJobHistory();
        assetJobHistory.setStatus(assetSubJobEntityFound.getStatus());
        assetJobHistory.setModifiedDate(timeNow);

        if (assetSubJobEntityFound.getHistory() != null) {
            assetSubJobEntityFound.getHistory().add(assetJobHistory);
        } else {
            assetSubJobEntityFound.setHistory(List.of(assetJobHistory));
        }
    }

    /**
     * This method updates asset sub job status based on the message received from Minio/Ml-asset
     * microservice. If a metadata file upload asset is complete it sends asset sub job message to
     * Ml-asset microservice for all the assets under the same parent job id. Ml-asset microservice
     * receives those messages & initiate upload to Minio.
     *
     * @param bucketEvent
     */
    public void updateSubJobStatusAndTriggerUploadAssets(BucketEvent bucketEvent) {
        if (!bucketEvent.isError()) {
            AssetEvent assetEvent = kafkaUtil.parseMinioEvent(bucketEvent);
            log.debug("Parsed Minio BucketEvent: {}", assetEvent);
            updateSubJobStatusForUploadSuccessAndAccumulatorIncrement(assetEvent);
        } else {
            log.warn("MinIO transfer ERROR, subJobId={},msg={}", bucketEvent.getSubJobId(), bucketEvent.getMessage());
            // If Asset upload is failed set asset subJob status ERROR
            var updateAssetSubJobDto = new AssetSubJobDto();
            updateAssetSubJobDto.setId(bucketEvent.getSubJobId());
            updateAssetSubJobDto.setStatus(AssetJobStatus.ERROR);
            updateAssetSubJobDto.setMessage(bucketEvent.getMessage());

            updateSubJobStatusForErrorAndAccumulatorIncrementAndUpdateJobStatus(updateAssetSubJobDto);
        }
    }

    /**
     * To update the sub-job status and enable stored field for upload success event
     * and increment the accumulator
     * if both stored and indexed are enabled.
     *
     * @param assetEvent
     */
    public void updateSubJobStatusForUploadSuccessAndAccumulatorIncrement(AssetEvent assetEvent) {
        String minioAssetUrl = assetConfigProperties.getMinioEndPoint() + assetEvent.getKey();

        // Indexing asset data before updating status to COMPLETE
        String metaDataFileUrl = assetEvent.getMetaDataFileUrl();

        // If Asset upload is successful set asset subJob status COMPLETE & set asset url
        var updateAssetSubJobDto = new AssetSubJobDto();
        updateAssetSubJobDto.setId(assetEvent.getSubJobId());
        updateAssetSubJobDto.setMetaDataFileUrl(metaDataFileUrl);

        // MongoDB Trigger will automatically change the sub-job status to COMPLETE if stored and indexed flags are enabled
        updateAssetSubJobDto.setUrl(minioAssetUrl);
        updateAssetSubJobDto.setMinioVersionId(assetEvent.getVersionId());
        updateAssetSubJobDto.setStored(true);

        // If the sub-job type is METADATA, we are enabling the indexed field manually,
        // as Elasticsearch does not produce indexed message for sub-job type METADATA
        if (assetEvent.getSubJobType() == AssetCategory.METADATA) {
            updateAssetSubJobDto.setIndexed(true);
        }

        // updating the sub-job status
        updateAndGetPrevStatus(updateAssetSubJobDto);

        log.debug("Accumulator-OK update,parent-job-id={},because of sub-job-id={}",
                assetEvent.getParentJobId(), assetEvent.getSubJobId());
    }

    /**
     * To update the sub-job status and enable indexed field for index success event
     * and increment the accumulator
     * if both stored and indexed are enabled.
     *
     * @param accumulatorEvent
     */
    void updateSubJobStatusForIndexSuccessAndAccumulatorIncrement(AccumulatorEvent accumulatorEvent) {
        var updateAssetSubJobDto = new AssetSubJobDto();
        updateAssetSubJobDto.setId(accumulatorEvent.getSubJobId());
        updateAssetSubJobDto.setIndexed(true);
        // updating the sub-job status
        updateAndGetPrevStatus(updateAssetSubJobDto);
    }

    private void updateSubJobStatusForErrorAndAccumulatorIncrementAndUpdateJobStatus(AssetSubJobDto updateAssetSubJobDto) {
        var prevAssetSubJobStatus = updateAndGetPrevStatus(updateAssetSubJobDto);
        if (prevAssetSubJobStatus == AssetJobStatus.IN_PROGRESS) { //count ERROR only once
            // updating the sub-jobs status count in AssetJobEntity for ProgressFailed
            var assetJobEntityAfterInc = updateSubJobStatusAccumulatorForProgressFailed(UUID.fromString(updateAssetSubJobDto.getId()));
            if (assetJobEntityAfterInc != null) {
                mongoDBOperationsRepository.updateAssetJobStatus(
                        assetJobEntityAfterInc.getId(), AssetJobStatus.ERROR.name());
            } else {
                throw new DataMoverException("SubJobId is missing in updateAssetSubJobDto %s".formatted(updateAssetSubJobDto));
            }
        }
    }

    /**
     * This method creates semantic version id for an asset
     *
     * @param assetName Name of the asset
     * @return Latest semantic version id
     */
    public int createSemanticVersionId(String assetName) {
        Optional<AssetSubJobEntity> foundLatestAsset =
                assetSubJobRepository.findFirstByFileNameAndStatusOrderBySemanticVersionIdDesc(
                        assetName, AssetJobStatus.COMPLETE.toString());

        if (foundLatestAsset.isPresent()) {
            log.debug("Asset details with latest version-id {}", foundLatestAsset);
            int lastSemanticVersionId = foundLatestAsset.get().getSemanticVersionId();

            if (lastSemanticVersionId == 0) {
                return INITIAL_SEMANTIC_VERSION;
            } else {
                return ++lastSemanticVersionId;
            }
        } else {
            log.debug("No previous asset details found with name: {}", assetName);
            return INITIAL_SEMANTIC_VERSION;
        }
    }

    /**
     * @param ids
     * @return
     */
    public List<AssetSubJobDto> findAllByIds(List<String> ids) {
        final Iterable<AssetSubJobEntity> assetSubJobEntities =
                assetSubJobRepository.findAllById(
                        ids.stream()
                                .map(AssetSubJobMapper::stringToUuid)
                                .collect(Collectors.toUnmodifiableList()));
        final List<AssetSubJobEntity> assetSubJobEntityList =
                StreamSupport.stream(assetSubJobEntities.spliterator(), false).collect(Collectors.toList());
        return assetSubJobMapper.toAssetSubJobDtoList(assetSubJobEntityList);
    }

    /**
     * To update the total sub-jobs count in in AssetJobEntity
     *
     * @param assetJobDto
     * @param totalCount
     */
    private void updatingTotalSubJobsCount(AssetJobDto assetJobDto, long totalCount) {
        var assetJobEntityOpt = assetJobRepository.findById(UUID.fromString(assetJobDto.getId()));
        if (assetJobEntityOpt.isPresent()) {
            assetJobEntityOpt.get()
                    .getSubJobStatusAccumulator()
                    .replace(AssetJobStatusAccumulator.TOTAL, totalCount);
            assetJobRepository.save(assetJobEntityOpt.get());
            log.info("Updated the total subJobs count for JobId - {}", assetJobDto.getId());
        } else {
            log.error("Update-total-count; missing Job.id={}", assetJobDto.getId());
        }
    }

    /**
     * To increment the sub-jobs status accumulator value for PROGRESS_OK in AssetJobEntity
     *
     * @param assetJobId
     */
    private AssetJobEntity updateSubJobStatusAccumulatorForProgressOK(UUID assetJobId) {
        return mongoDBOperationsRepository.incrementSubJobsStatusCount(
                assetJobId, "subJobStatusAccumulator.PROGRESS_OK", 1L);
    }

    /**
     * To increment the sub-jobs status accumulator value for PROGRESS_FAILED in AssetJobEntity
     *
     * @param subJobId
     */
    private AssetJobEntity updateSubJobStatusAccumulatorForProgressFailed(UUID subJobId) {
        //TODO: performance optimization. findById loads the full object, instead only of parentJobID
        var assetSubJobEntityOpt = assetSubJobRepository.findById(subJobId);
        return assetSubJobEntityOpt.map(assetSubJobEntity -> mongoDBOperationsRepository.incrementSubJobsStatusCount(
                assetSubJobEntity.getParentJobId(), "subJobStatusAccumulator.PROGRESS_FAILED", 1L)).orElse(null);
    }


    @SneakyThrows
    public void sendAssetMetadataMessageToKafka(AssetMetadata assetMetadata) {
        kafkaProducer.sendMessage(assetConfigProperties.getIndexMetadataTopic(), assetMetadata);
    }

    public void updateJobStatusToError(AssetJobDto assetJobDto, String errorMessage) {
        log.debug("jobId={},Updating AssetJob Status to ERROR due to following reason: {}", assetJobDto.getId(), errorMessage);
        var assetJobEntityFound =
                assetJobRepository.findById(UUID.fromString(assetJobDto.getId()));
        if (assetJobEntityFound.isPresent()) {
            var assetJobEntity = assetJobEntityFound.get();
            assetJobEntity.setStatus(AssetJobStatus.ERROR);
            assetJobEntity.setMessage(errorMessage);
            assetJobRepository.save(assetJobEntity);
        }
    }

    /**
     * To update bulk metadata
     *
     * @param updateBulkMetadataRequestDto
     * @return AssetJobDto
     */
    public RetrieveAssetJobDto updateBulkMetadata(UpdateBulkMetadataRequestDto updateBulkMetadataRequestDto,
                                                  List<String> userRoles) {
        Assert.notEmpty(updateBulkMetadataRequestDto.getAssetMetadata(), "At least 1 element of Asset Metadata is required");
        var assetJobEntity = assetJobRepository.findById(updateBulkMetadataRequestDto.getParentJobId());
        var parentJobId = updateBulkMetadataRequestDto.getParentJobId().toString();
        if (assetJobEntity.isPresent() && assetJobEntity.get().getStatus() == AssetJobStatus.IN_PROGRESS) {
            // Total no. of asset sub-job capacity for the given parentJobId
            var totalSubJobCount = assetJobEntity.get().getSubJobStatusAccumulator().get(AssetJobStatusAccumulator.TOTAL);
            // Total no. of asset sub-job has been created till date for the given parentJobId
            var totalSubJobCreatedCount = assetSubJobRepository.countByParentJobId(UUID.fromString(parentJobId));
            // Total no. of asset metadata is being requested to create as a sub-job
            var totalAssetMetadataCount = updateBulkMetadataRequestDto.getAssetMetadata().size();
            // Total no. of asset sub-job is being requested should be less than or equal to
            // available asset sub-job capacity for a given parentJobId
            if ((totalSubJobCount - totalSubJobCreatedCount) < totalAssetMetadataCount) {
                throw new DataMoverException(MessageFormat.format("Asset job id = {0} with accumulators total = ({1} / {2}) has no space for new asset metadata count = {3}", parentJobId, totalSubJobCreatedCount, totalSubJobCount, totalAssetMetadataCount));
            }
            var timestamp = Calendar.getInstance().getTime();
            for (com.xperi.datamover.dto.metadata.AssetMetadata assetMetadata : updateBulkMetadataRequestDto.getAssetMetadata()) {
                final var assetSubJobDto = createAssetSubJobDto(parentJobId, assetJobEntity.get().getJobName(), timestamp);
                assetMetadata.setUserRoles(userRoles);
                var metadataTask = new MetadataTask(this, assetSubJobDto, assetJobMapper.toAssetMetadataSchema(assetMetadata), userRoles);
                submitForAsyncMetadataExecution(metadataTask);
            }
            return assetJobMapper.retrieveAssetJobDtoFromJobEntity(assetJobEntity.get());
        }
        throw new DataMoverException("The given asset job id doesn't exists or it's already completed: " + parentJobId);
    }

    /**
     * To create a asset sub job dto
     *
     * @param parentJobId Parent job id for the sub-job
     * @param jobName     name of the asset job
     * @param timestamp   timestamp for the creation of asset sub-job
     * @return AssetSubJobDto
     */
    private AssetSubJobDto createAssetSubJobDto(String parentJobId, String jobName, Date timestamp) {
        final var assetSubJobDto = new AssetSubJobDto();
        assetSubJobDto.setStatus(AssetJobStatus.IN_PROGRESS);
        assetSubJobDto.setParentJobId(parentJobId);
        assetSubJobDto.setType(AssetCategory.METADATA);
        assetSubJobDto.setJobName(jobName);
        assetSubJobDto.setStored(true);
        assetSubJobDto.setCreatedAt(timestamp);
        assetSubJobDto.setUpdatedAt(timestamp);
        return assetSubJobDto;
    }

    private boolean isIgnoreFilename(String filename) {
        for (var pattern : filenameFilterPatterns) {
            if (pattern.matcher(filename).matches()) {
                return true;
            }
        }
        return false;
    }


}
