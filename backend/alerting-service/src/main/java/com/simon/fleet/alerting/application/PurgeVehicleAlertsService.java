package com.simon.fleet.alerting.application;

import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.in.PurgeVehicleAlertsUseCase;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.domain.port.out.AlertRepositoryPort;
import com.simon.fleet.alerting.domain.port.out.VehicleTrackingStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurgeVehicleAlertsService implements PurgeVehicleAlertsUseCase {

    private final AlertRepositoryPort alertRepositoryPort;
    private final VehicleTrackingStatePort trackingStatePort;
    private final AlertEventPublisherPort eventPublisherPort;

    @Override
    public void purge(VehiclePlate plate) {
        alertRepositoryPort.purgeByVehicle(plate);
        trackingStatePort.clear(plate);
        eventPublisherPort.publishDataPurged(plate);
    }
}
