package com.simon.fleet.ingestion.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa el payload JSON tal como lo define el enunciado de la prueba: claves en
 * snake_case ({@code vehicle_id}), no en camelCase. Es puramente un contrato HTTP; se traduce
 * a {@code TelemetryPoint} en {@link TelemetryRequestMapper} antes de entrar al dominio.
 */
@Schema(description = "Lectura GPS enviada por un vehículo")
public record TelemetryRequestDto(
        @Schema(description = "Identificador del vehículo", example = "v1")
        @JsonProperty("vehicle_id") String vehicleId,

        @Schema(description = "Latitud en grados decimales", example = "4.6")
        Double lat,

        @Schema(description = "Longitud en grados decimales", example = "-74.08")
        Double lng,

        @Schema(description = "Momento de la lectura, ISO-8601", example = "2026-07-03T21:35:44.172Z")
        String timestamp
) {
}
