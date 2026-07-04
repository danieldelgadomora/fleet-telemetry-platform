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

@Service
@RequiredArgsConstructor
public class IngestTelemetryService implements IngestTelemetryUseCase {

    private final TelemetryValidationChain validationChain;
    private final TelemetryDeduplicationPort deduplicationPort;
    private final TelemetryCachePort cachePort;
    private final TelemetryEventPublisherPort eventPublisherPort;

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
