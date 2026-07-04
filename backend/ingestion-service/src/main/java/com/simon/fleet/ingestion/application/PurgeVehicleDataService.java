package com.simon.fleet.ingestion.application;

import com.simon.fleet.ingestion.domain.model.VehicleId;
import com.simon.fleet.ingestion.domain.port.in.PurgeVehicleDataUseCase;
import com.simon.fleet.ingestion.domain.port.out.TelemetryCachePort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryEventPublisherPort;
import com.simon.fleet.ingestion.domain.port.out.TelemetryHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurgeVehicleDataService implements PurgeVehicleDataUseCase {

    private final TelemetryCachePort cachePort;
    private final TelemetryHistoryRepositoryPort historyRepositoryPort;
    private final TelemetryEventPublisherPort eventPublisherPort;

    @Override
    public void purgeVehicle(VehicleId vehicleId) {
        cachePort.clearVehicleCache(vehicleId);
        historyRepositoryPort.purgeByVehicle(vehicleId);
        eventPublisherPort.publishCacheCleared(vehicleId);
    }
}
