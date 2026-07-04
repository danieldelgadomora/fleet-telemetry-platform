package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.BroadcastAlertUseCase;
import com.simon.fleet.gateway.domain.port.out.AlertBroadcastPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BroadcastAlertService implements BroadcastAlertUseCase {

    private final AlertBroadcastPort alertBroadcastPort;

    @Override
    public void broadcastAlert(VehicleId vehicleId, String alertId, String ruleCode, String message, Instant raisedAt) {
        alertBroadcastPort.broadcastAlert(vehicleId, alertId, ruleCode, message, raisedAt);
    }
}
