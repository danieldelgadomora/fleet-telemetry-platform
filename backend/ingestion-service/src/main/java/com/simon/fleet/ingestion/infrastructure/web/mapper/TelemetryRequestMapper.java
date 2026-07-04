package com.simon.fleet.ingestion.infrastructure.web.mapper;

import com.simon.fleet.ingestion.domain.model.Coordinates;
import com.simon.fleet.ingestion.domain.exception.InvalidTelemetryPayloadException;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehicleId;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryRequestDto;

import java.time.DateTimeException;
import java.time.Instant;

/**
 * Traduce el DTO HTTP (texto suelto, puede venir mal formado) al modelo de dominio (siempre
 * válido). Cualquier problema de formato -campo faltante, número inválido, timestamp que no
 * es ISO-8601- se normaliza aquí como {@link InvalidTelemetryPayloadException}, para que el
 * resto del sistema nunca tenga que lidiar con datos crudos a medio parsear.
 */
public final class TelemetryRequestMapper {

    private TelemetryRequestMapper() {
    }

    public static TelemetryPoint toDomain(TelemetryRequestDto dto) {
        if (dto.vehicleId() == null || dto.vehicleId().isBlank()) {
            throw new InvalidTelemetryPayloadException("vehicle_id es obligatorio");
        }
        if (dto.lat() == null || dto.lng() == null) {
            throw new InvalidTelemetryPayloadException("lat y lng son obligatorios");
        }
        if (dto.timestamp() == null || dto.timestamp().isBlank()) {
            throw new InvalidTelemetryPayloadException("timestamp es obligatorio");
        }

        try {
            VehicleId vehicleId = new VehicleId(dto.vehicleId());
            Coordinates coordinates = new Coordinates(dto.lat(), dto.lng());
            Instant recordedAt = Instant.parse(dto.timestamp());
            return new TelemetryPoint(vehicleId, coordinates, recordedAt);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new InvalidTelemetryPayloadException("payload inválido: " + e.getMessage());
        }
    }
}
