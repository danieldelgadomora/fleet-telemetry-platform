package com.simon.fleet.ingestion.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface TelemetryMongoRepository extends MongoRepository<TelemetryDocument, String> {

    void deleteByVehicleId(String vehicleId);
}
