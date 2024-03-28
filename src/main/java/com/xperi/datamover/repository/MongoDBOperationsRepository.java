package com.xperi.datamover.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOneModel;
import com.xperi.datamover.config.MongoConfig;
import com.xperi.datamover.dto.AssetSubJobDto;
import com.xperi.datamover.entity.AssetJobEntity;
import com.xperi.datamover.entity.AssetSubJobEntity;
import com.xperi.datamover.util.AssetSubJobMapper;
import org.bson.BsonDocument;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MongoDBOperationsRepository {

  @Autowired private MongoTemplate mongoTemplate;

  @Autowired private MongoConfig mongoConfig;

  private static final String[] includeFields = {"subJobStatusAccumulator.TOTAL", "subJobStatusAccumulator.PROGRESS_OK",
          "subJobStatusAccumulator.PROGRESS_FAILED"};
  /**
   * To increment the sub-jobs status accumulator value in AssetJobEntity
   *
   * @param assetJobId
   * @param key
   * @param value
   */
  public AssetJobEntity incrementSubJobsStatusCount(UUID assetJobId, String key, long value) {
    Query query = new Query(Criteria.where("id").is(assetJobId));
    query.fields().include(includeFields);
    Update update = new Update().inc(key, value);
    return mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true), AssetJobEntity.class);
  }

  public AssetSubJobEntity updateSubJobAndStatus(AssetSubJobDto assetSubJobDto) {
    var query = new Document("_id", AssetSubJobMapper.stringToUuid(assetSubJobDto.getId()));

    List<Document> pipelineUpdate = new ArrayList<>();

    // Stage 1 - Setting value of Indexed and Stored
    var setIndexedStage = new Document("$set",
            new Document("indexed",
                new Document("$or", Arrays.asList("$indexed", assetSubJobDto.isIndexed()))));
    pipelineUpdate.add(setIndexedStage);

    var setStoredStage = new Document("$set", new Document("stored",
            new Document("$or", Arrays.asList("$stored",assetSubJobDto.isStored()))));
    pipelineUpdate.add(setStoredStage);

    var setUpdatedAtStage = new Document("$set",
            new Document("updatedAt", Calendar.getInstance().getTime()));
    pipelineUpdate.add(setUpdatedAtStage);

    /*Required to set the value of MinioVersionId if value is passed in the DTO */
    if(assetSubJobDto.getMinioVersionId() != null) {
      var setMinioVersionIdStage = new Document("$set",
              new Document("minioVersionId", assetSubJobDto.getMinioVersionId()));
              pipelineUpdate.add(setMinioVersionIdStage);
    }

    // Stage 3 - Evaluating if Indexed and Stored are true
    var isIndexedAndStoredTrue = new Document("$eq", Arrays.asList(
            new Document("$and", Arrays.asList("$indexed", "$stored")),
            true
    ));

    /*Stage 4 - Setting the value of status depending on the value of
     * isIndexedAndStoredTrue evaluated in the previous stage
     */
    var setStatusStage = new Document("$set", new Document("status",
            new Document("$cond",
                    new Document("if", isIndexedAndStoredTrue)
                            .append("then", "COMPLETE")
                            .append("else", "$status"))
    ));
    pipelineUpdate.add(setStatusStage);

    UpdateOneModel<BsonDocument> updateOneModel = new UpdateOneModel<>(query, pipelineUpdate);

    MongoCollection<AssetSubJobEntity> assetSubJobEntityMongoCollection =
            mongoConfig.getAssetSubJobEntityMongoCollection();

    return assetSubJobEntityMongoCollection.findOneAndUpdate(
            updateOneModel.getFilter(),
            updateOneModel.getUpdatePipeline(),
            new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.BEFORE));
  }

  public void updateAssetJobStatus(UUID assetJobId, String status){
    Query query = new Query(Criteria.where("id").is(assetJobId));
    Update update = new Update().set("status", status);
    mongoTemplate.updateFirst(query, update, AssetJobEntity.class);
  }
}

