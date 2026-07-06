package com.simon.fleet.ingestion.infrastructure.web.controller;

import com.simon.fleet.ingestion.domain.port.in.GetTelemetryHistoryUseCase;
import com.simon.fleet.ingestion.domain.port.in.IngestTelemetryUseCase;
import com.simon.fleet.ingestion.domain.model.TelemetryIngestionResult;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryHistoryPointDto;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryRequestDto;
import com.simon.fleet.ingestion.infrastructure.web.dto.TelemetryResponseDto;
import com.simon.fleet.ingestion.infrastructure.web.mapper.TelemetryHistoryMapper;
import com.simon.fleet.ingestion.infrastructure.web.mapper.TelemetryRequestMapper;
import com.simon.fleet.ingestion.infrastructure.web.mapper.TelemetryResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Punto de entrada de la telemetría GPS (Servicio de Ingesta). El POST responde rápido: valida,
 * deduplica y cachea la posición, pero la persistencia histórica en MongoDB ocurre después,
 * de forma asíncrona vía RabbitMQ (ver {@code TelemetryPersistenceConsumer}). El GET de
 * historial, en cambio, sí lee directamente de esa persistencia histórica ya escrita, para que
 * el dashboard pueda trazar el recorrido de un vehículo.
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetría", description = "Ingesta de coordenadas GPS de vehículos")
public class TelemetryController {

    private static final int DEFAULT_HISTORY_LIMIT = 200;

    private final IngestTelemetryUseCase ingestTelemetryUseCase;
    private final GetTelemetryHistoryUseCase getTelemetryHistoryUseCase;

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
        // u otra); el cuerpo sí distingue cuál fue, para que el cliente sepa qué pasó.
        TelemetryResponseDto body = TelemetryResponseMapper.toDto(point, result);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @GetMapping("/{plate}/history")
    @Operation(
            summary = "Histórico reciente de recorrido de un vehículo",
            description = """
                    Las lecturas más recientes de la placa, en orden cronológico (la más vieja
                    primero) para poder trazarlas directamente como una ruta. No valida que la
                    placa exista: ingestion-service no es dueño de esa identidad, así que una
                    placa que nunca reportó telemetría simplemente devuelve una lista vacía.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Histórico (posiblemente vacío) de la placa")
    public ResponseEntity<List<TelemetryHistoryPointDto>> history(
            @PathVariable String plate,
            @Parameter(description = "Cantidad máxima de puntos a devolver")
            @RequestParam(defaultValue = "" + DEFAULT_HISTORY_LIMIT) int limit) {
        List<TelemetryPoint> points = getTelemetryHistoryUseCase.history(new VehiclePlate(plate), limit);
        return ResponseEntity.ok(TelemetryHistoryMapper.toDto(points));
    }
}
