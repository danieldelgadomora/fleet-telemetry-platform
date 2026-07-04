package com.simon.fleet.ingestion.infrastructure.persistence.mongo;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TimeSeries;

import java.time.Instant;

/**
 * Documento de la colección time-series {@code telemetry_history}: una fila por lectura GPS,
 * append-only. {@code metaField = vehicleId} le dice a MongoDB que agrupe internamente las
 * lecturas por vehículo, lo que hace muy eficientes las consultas por vehículo + rango de
 * tiempo (el patrón de acceso principal de este histórico).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = TelemetryDocument.COLLECTION)
@TimeSeries(timeField = "recordedAt", metaField = "vehicleId", collection = TelemetryDocument.COLLECTION)
public class TelemetryDocument {

    static final String COLLECTION = "telemetry_history";

    @Id
    private String id;
    private String vehicleId;
    private double lat;
    private double lng;
    private Instant recordedAt;

    /** Mismo punto que lat/lng, en formato GeoJSON (orden lng,lat) para el índice 2dsphere. */
    private GeoJsonPoint location;

    public static TelemetryDocument fromDomain(TelemetryPoint point) {
        return new TelemetryDocument(
                null,
                point.vehicleId().value(),
                point.coordinates().lat(),
                point.coordinates().lng(),
                point.recordedAt(),
                new GeoJsonPoint(point.coordinates().lng(), point.coordinates().lat())
        );
    }
}
