package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.in.HandleCacheClearedUseCase;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Consume la confirmación de limpieza de caché/Mongo que publica ingestion-service, una de las
 * dos confirmaciones que necesita la Saga de eliminación para completarse.
 */
@Service
@RequiredArgsConstructor
public class HandleCacheClearedService implements HandleCacheClearedUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final FleetStatusBroadcastPort fleetStatusBroadcastPort;

    /** Registra la confirmación y, si con esta ya se cumplieron ambas, cierra la Saga (DELETED) y lo empuja al dashboard. */
    @Override
    public void onCacheCleared(VehiclePlate plate, Instant clearedAt) {
        repositoryPort.markCacheCleared(plate, clearedAt);
        // Atómico y condicionado en SQL a que ambas confirmaciones ya estén: no importa si
        // esta llega antes o después que la de alerting-service, ni si ambos consumers
        // corren al mismo tiempo. Como HandleDataPurgedService hace la misma llamada, solo una
        // de las dos puede ganar la condición SQL, así que el broadcast de DELETED ocurre
        // exactamente una vez.
        if (repositoryPort.completeIfBothConfirmed(plate)) {
            repositoryPort.findById(plate).ifPresent(fleetStatusBroadcastPort::broadcastStatus);
        }
    }
}
