package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.port.in.PersistTelemetryHistoryUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersistTelemetryHistoryService implements PersistTelemetryHistoryUseCase {

    private final TelemetryHistoryRepositoryPort historyRepositoryPort;

    @Override
    public void persist(TelemetryPoint point) {
        historyRepositoryPort.save(point);
    }
}
