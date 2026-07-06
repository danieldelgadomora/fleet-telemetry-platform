package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.TelemetryPoint;
import com.simon.fleet.ingestion.domain.port.in.PersistTelemetryHistoryUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Consumer del evento de telemetría recibida: persiste el histórico GPS en MongoDB, protegido
 * por Circuit Breaker/Retry a nivel del adaptador para que un Mongo caído no tumbe la ingesta.
 */
@Service
@RequiredArgsConstructor
public class PersistTelemetryHistoryService implements PersistTelemetryHistoryUseCase {

    private final TelemetryHistoryRepositoryPort historyRepositoryPort;

    /** Guarda la lectura en el histórico de telemetría de la placa. */
    @Override
    public void persist(TelemetryPoint point) {
        historyRepositoryPort.save(point);
    }
}
