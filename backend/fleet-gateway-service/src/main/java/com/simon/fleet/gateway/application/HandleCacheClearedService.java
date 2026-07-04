package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.HandleCacheClearedUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HandleCacheClearedService implements HandleCacheClearedUseCase {

    private final VehicleRepositoryPort repositoryPort;

    @Override
    public void onCacheCleared(VehicleId vehicleId, Instant clearedAt) {
        repositoryPort.markCacheCleared(vehicleId, clearedAt);
        // Atómico y condicionado en SQL a que ambas confirmaciones ya estén: no importa si
        // esta llega antes o después que la de alerting-service, ni si ambos consumers
        // corren al mismo tiempo.
        repositoryPort.completeIfBothConfirmed(vehicleId);
    }
}
