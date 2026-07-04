package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq;

import com.simon.fleet.contracts.alert.VehicleAlertRaisedEvent;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.HandleVehicleAlertRaisedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Primer consumer de {@code fleet.alerts}: hasta ahora alerting-service publicaba estas
 * alertas sin que nadie las escuchara. Actualiza la vista de lectura del dashboard a ALERTA.
 */
@Component
@RequiredArgsConstructor
public class FleetStatusAlertConsumer {

    private final HandleVehicleAlertRaisedUseCase handleVehicleAlertRaisedUseCase;

    @RabbitListener(queues = RabbitMqConfig.FLEET_STATUS_ALERT_QUEUE)
    public void onAlertRaised(VehicleAlertRaisedEvent event) {
        handleVehicleAlertRaisedUseCase.onAlertRaised(new VehicleId(event.vehicleId()), event.raisedAt());
    }
}
