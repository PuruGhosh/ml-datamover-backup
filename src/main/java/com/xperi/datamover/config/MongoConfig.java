package com.xperi.datamover.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.xperi.datamover.entity.AssetSubJobEntity;
import lombok.RequiredArgsConstructor;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/** This class specifies the Spring Data Mongo configuration */
@Configuration
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

  private final MongoProperties mongoProperties;

  private static final String ASSET_SUB_JOBS_COLLECTION = "assetSubJobs";

  private MongoCollection<AssetSubJobEntity> assetSubJobEntityMongoCollection;

  /**
   * This method returns the mongo connection
   *
   * @return MongoClient
   */
  @Override
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString(mongoProperties.getUri());
    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(connectionString)
            .build();
    return MongoClients.create(mongoClientSettings);
  }

  @Override
  protected String getDatabaseName() {
    return mongoProperties.getDatabase();
  }

  @Bean
  MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
  }

  /**
   * This method returns the Singleton AssetSubJobEntity Mongo Collection object
   *
   * @return MongoCollection<AssetSubJobEntity>
   */
  public MongoCollection<AssetSubJobEntity> getAssetSubJobEntityMongoCollection(){
    CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(AssetSubJobEntity.class).build();
    CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    if(assetSubJobEntityMongoCollection == null){
      synchronized (this){
        assetSubJobEntityMongoCollection = mongoClient()
                .getDatabase(mongoProperties.getDatabase())
                .getCollection(ASSET_SUB_JOBS_COLLECTION, AssetSubJobEntity.class)
                .withCodecRegistry(pojoCodecRegistry);
      }
    }
    return assetSubJobEntityMongoCollection;
  }
}
