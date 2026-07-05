package com.simon.fleet.ingestion.infrastructure.web.controller;

import com.simon.fleet.ingestion.domain.port.in.IngestTelemetryUseCase;
import com.simon.fleet.ingestion.domain.model.TelemetryIngestionResult;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryRequestDto;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryResponseDto;
import com.simon.fleet.ingestion.infrastructure.web.mapper.TelemetryRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada de la telemetría GPS (Servicio de Ingesta). Responde rápido: valida,
 * deduplica y cachea la posición, pero la persistencia histórica en MongoDB ocurre después,
 * de forma asíncrona vía RabbitMQ (ver {@code TelemetryPersistenceConsumer}).
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetría", description = "Ingesta de coordenadas GPS de vehículos")
public class TelemetryController {

    private final IngestTelemetryUseCase ingestTelemetryUseCase;

    @PostMapping
    @Operation(
            summary = "Recibe una lectura GPS de un vehículo",
            description = """
                    Valida el payload, ignora duplicados (misma coordenada del mismo vehículo
                    dentro de la ventana de dedupe) y cachea la última posición conocida.
                    La persistencia en MongoDB y la evaluación de alertas ocurren después, de
                    forma asíncrona.
                    """
    )
    @ApiResponse(responseCode = "202", description = "Lectura aceptada (nueva o duplicado ignorado)")
    @ApiResponse(responseCode = "400", description = "Payload inválido: campo faltante, fuera de rango o timestamp no parseable")
    public ResponseEntity<TelemetryResponseDto> receive(@RequestBody TelemetryRequestDto request) {
        TelemetryPoint point = TelemetryRequestMapper.toDomain(request);
        TelemetryIngestionResult result = ingestTelemetryUseCase.ingest(point);

        // Ambos resultados son un 202 (el dato ya quedó representado en el sistema de una forma
        // u otra), pero el cuerpo sí distingue cuál fue, para que el cliente sepa qué pasó.
        String message = switch (result) {
            case ACCEPTED -> "Telemetría aceptada, la persistencia histórica ocurre de forma asíncrona";
            case DUPLICATE_IGNORED -> "Lectura duplicada dentro de la ventana de dedupe, ignorada";
        };

        TelemetryResponseDto body = new TelemetryResponseDto(point.plate().value(), result.name(), message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
