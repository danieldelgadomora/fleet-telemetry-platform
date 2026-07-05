package com.simon.fleet.ingestion.infrastructure.web.mapper;

import com.simon.fleet.ingestion.domain.exception.InvalidPanicPayloadException;
import com.simon.fleet.ingestion.domain.model.PanicButtonPress;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.infrastructure.web.dto.PanicRequestDto;

import java.time.DateTimeException;
import java.time.Instant;

/**
 * Traduce el DTO HTTP del botón de pánico al modelo de dominio. A diferencia de
 * {@code TelemetryRequestMapper}, no valida rango de coordenadas: son opcionales y, si vienen,
 * se propagan tal cual, porque perder precisión de la última posición conocida es preferible a
 * rechazar una activación de pánico por un problema de formato de coordenadas.
 */
public final class PanicRequestMapper {

    private PanicRequestMapper() {
    }

    public static PanicButtonPress toDomain(PanicRequestDto dto) {
        if (dto.plate() == null || dto.plate().isBlank()) {
            throw new InvalidPanicPayloadException("plate es obligatorio");
        }
        if (dto.timestamp() == null || dto.timestamp().isBlank()) {
            throw new InvalidPanicPayloadException("timestamp es obligatorio");
        }

        try {
            VehiclePlate plate = new VehiclePlate(dto.plate());
            Instant triggeredAt = Instant.parse(dto.timestamp());
            String message = (dto.message() == null || dto.message().isBlank()) ? null : dto.message().trim();
            return new PanicButtonPress(plate, dto.lat(), dto.lng(), message, triggeredAt);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new InvalidPanicPayloadException("payload inválido: " + e.getMessage());
        }
    }
}
