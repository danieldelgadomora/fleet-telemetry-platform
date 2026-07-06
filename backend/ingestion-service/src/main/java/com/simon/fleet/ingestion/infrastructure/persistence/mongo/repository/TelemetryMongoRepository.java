package com.simon.fleet.ingestion.infrastructure.persistence.mongo.repository;

import com.simon.fleet.ingestion.infrastructure.persistence.mongo.entity.TelemetryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio Spring Data MongoDB de la colección de histórico de telemetría. Spring Data
 * genera la implementación automáticamente por extender {@link MongoRepository}; no necesita
 * {@code @Repository} explícita.
 */
public interface TelemetryMongoRepository extends MongoRepository<TelemetryDocument, String> {

    /** Borra el histórico de telemetría de la placa (participante de la Saga de eliminación). */
    void deleteByPlate(String plate);
}
