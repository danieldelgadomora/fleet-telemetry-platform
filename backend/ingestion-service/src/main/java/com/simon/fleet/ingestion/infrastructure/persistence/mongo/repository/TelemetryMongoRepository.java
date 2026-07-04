package com.simon.fleet.ingestion.infrastructure.persistence.mongo.repository;

import com.simon.fleet.ingestion.infrastructure.persistence.mongo.entity.TelemetryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TelemetryMongoRepository extends MongoRepository<TelemetryDocument, String> {

    void deleteByVehicleId(String vehicleId);
}
