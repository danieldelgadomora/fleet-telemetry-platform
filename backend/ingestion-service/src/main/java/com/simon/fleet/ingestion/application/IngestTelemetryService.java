package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.TelemetryIngestionResult;
import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.port.in.IngestTelemetryUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryCachePort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryDeduplicationPort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import com.simon.fleet.ingestion.domain.validation.TelemetryValidationChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Punto de entrada de la ingesta: valida la lectura GPS (Chain of Responsibility), la descarta
 * de forma idempotente si es un duplicado reciente, y si no lo es, actualiza la última posición
 * conocida en caché y publica el evento para que alerting-service y fleet-gateway-service lo
 * consuman — nunca persiste el histórico directamente (eso lo hace el consumer async, protegido
 * por Circuit Breaker).
 */
@Service
@RequiredArgsConstructor
public class IngestTelemetryService implements IngestTelemetryUseCase {

    private final TelemetryValidationChain validationChain;
    private final TelemetryDeduplicationPort deduplicationPort;
    private final TelemetryCachePort cachePort;
    private final TelemetryEventPublisherPort eventPublisherPort;

    /**
     * Valida la lectura y la descarta si ya se recibió una igual dentro de la ventana de
     * dedupe; si es nueva, actualiza la última posición conocida y publica el evento.
     */
    @Override
    public TelemetryIngestionResult ingest(TelemetryPoint point) {
        validationChain.validate(point);

        if (deduplicationPort.isDuplicate(point)) {
            return TelemetryIngestionResult.DUPLICATE_IGNORED;
        }

        cachePort.saveLastKnownPosition(point);
        eventPublisherPort.publishReceived(point);
        return TelemetryIngestionResult.ACCEPTED;
    }
}
