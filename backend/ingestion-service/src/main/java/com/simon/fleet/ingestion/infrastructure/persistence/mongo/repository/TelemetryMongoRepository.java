package com.simon.fleet.ingestion.infrastructure.persistence.mongo.repository;

import com.simon.fleet.ingestion.infrastructure.persistence.mongo.entity.TelemetryDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repositorio Spring Data MongoDB de la colección de histórico de telemetría. Spring Data
 * genera la implementación automáticamente por extender {@link MongoRepository}; no necesita
 * {@code @Repository} explícita.
 */
public interface TelemetryMongoRepository extends MongoRepository<TelemetryDocument, String> {

    /** Borra el histórico de telemetría de la placa (participante de la Saga de eliminación). */
    void deleteByPlate(String plate);

    /**
     * Las lecturas más recientes de una placa, de más nueva a más vieja. {@code plate} ya es el
     * {@code metaField} de la colección time-series, así que Mongo ya bucketiza internamente por
     * este campo — no hace falta un índice adicional para que esta consulta sea eficiente.
     */
    List<TelemetryDocument> findByPlateOrderByRecordedAtDesc(String plate, Pageable pageable);
}
