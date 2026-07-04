package com.simon.fleet.ingestion.infrastructure.persistence.mongo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.stereotype.Component;

/**
 * Se asegura de que la colección time-series y su índice geoespacial existan al arrancar.
 * Spring Data Mongo crea la colección con las opciones de {@code @TimeSeries} solo cuando se
 * pide explícitamente vía {@code createCollection}; si ya existe (por ejemplo, en un
 * reinicio), simplemente no hace nada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureTelemetryHistoryCollectionExists() {
        if (!mongoTemplate.collectionExists(TelemetryDocument.class)) {
            mongoTemplate.createCollection(TelemetryDocument.class);
            log.info("Colección time-series '{}' creada", TelemetryDocument.COLLECTION);
        }

        mongoTemplate.indexOps(TelemetryDocument.class)
                .ensureIndex(new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE));
    }
}
