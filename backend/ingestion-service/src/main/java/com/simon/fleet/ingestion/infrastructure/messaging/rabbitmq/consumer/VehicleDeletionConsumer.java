package com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.contracts.lifecycle.VehicleDeletionRequestedEvent;
import com.simon.fleet.ingestion.domain.port.in.PurgeVehicleDataUseCase;
import com.simon.fleet.ingestion.domain.model.VehicleId;
import com.simon.fleet.ingestion.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Lado consumidor, en ingestion-service, de la Saga de borrado de vehículo. Escucha el evento
 * que publica fleet-gateway-service al iniciar el borrado, y limpia la parte que le
 * corresponde (caché Redis + histórico Mongo) vía {@link PurgeVehicleDataUseCase}.
 */
@Component
@RequiredArgsConstructor
public class VehicleDeletionConsumer {

    private final PurgeVehicleDataUseCase purgeVehicleDataUseCase;

    @RabbitListener(queues = RabbitMqConfig.VEHICLE_DELETION_QUEUE)
    public void onVehicleDeletionRequested(VehicleDeletionRequestedEvent event) {
        purgeVehicleDataUseCase.purgeVehicle(new VehicleId(event.vehicleId()));
    }
}
