package com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.alerting.domain.port.in.PurgeVehicleAlertsUseCase;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import com.simon.fleet.contracts.lifecycle.VehicleDeletionRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Lado consumidor, en alerting-service, de la Saga de borrado de vehículo: escucha el evento
 * que publica fleet-gateway-service al iniciar el borrado, y purga la parte que le
 * corresponde (alertas en Postgres + estado de tracking en Redis) vía
 * {@link PurgeVehicleAlertsUseCase}.
 */
@Component
@RequiredArgsConstructor
public class VehicleDeletionConsumer {

    private final PurgeVehicleAlertsUseCase purgeVehicleAlertsUseCase;

    @RabbitListener(queues = RabbitMqConfig.VEHICLE_DELETION_QUEUE)
    public void onVehicleDeletionRequested(VehicleDeletionRequestedEvent event) {
        purgeVehicleAlertsUseCase.purge(new VehiclePlate(event.plate()));
    }
}
