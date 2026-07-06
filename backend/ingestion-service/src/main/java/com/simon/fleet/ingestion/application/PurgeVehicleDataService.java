package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.VehiclePlate;
import com.simon.fleet.ingestion.domain.port.in.PurgeVehicleDataUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryCachePort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Participante de la Saga de eliminación del lado de ingestion-service: al recibir la solicitud
 * de borrado, limpia la caché Redis (última posición, claves de dedupe) y el histórico de
 * telemetría en Mongo de la placa, y confirma la limpieza publicando el evento correspondiente
 * para que fleet-gateway-service pueda completar la Saga.
 */
@Service
@RequiredArgsConstructor
public class PurgeVehicleDataService implements PurgeVehicleDataUseCase {

    private final TelemetryCachePort cachePort;
    private final TelemetryHistoryRepositoryPort historyRepositoryPort;
    private final TelemetryEventPublisherPort eventPublisherPort;

    /** Limpia la caché y el histórico de la placa, y confirma la limpieza. */
    @Override
    public void purgeVehicle(VehiclePlate plate) {
        cachePort.clearVehicleCache(plate);
        historyRepositoryPort.purgeByVehicle(plate);
        eventPublisherPort.publishCacheCleared(plate);
    }
}
