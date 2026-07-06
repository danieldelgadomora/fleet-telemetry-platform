package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.in.PurgeVehicleAlertsUseCase;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import com.simon.fleet.alerting.domain.port.out.VehicleTrackingStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Participante de la Saga de eliminación del lado de alerting-service: al recibir la solicitud
 * de borrado, purga el histórico de alertas y el estado de tracking (Redis) de la placa, y
 * confirma la limpieza publicando el evento correspondiente para que fleet-gateway-service
 * pueda completar la Saga.
 */
@Service
@RequiredArgsConstructor
public class PurgeVehicleAlertsService implements PurgeVehicleAlertsUseCase {

    private final AlertRepositoryPort alertRepositoryPort;
    private final VehicleTrackingStatePort trackingStatePort;
    private final AlertEventPublisherPort eventPublisherPort;

    /** Purga las alertas y el estado de tracking de la placa, y confirma la limpieza. */
    @Override
    public void purge(VehiclePlate plate) {
        alertRepositoryPort.purgeByVehicle(plate);
        trackingStatePort.clear(plate);
        eventPublisherPort.publishDataPurged(plate);
    }
}
