package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq;

import com.simon.fleet.contracts.lifecycle.VehicleDataPurgedEvent;
import com.simon.fleet.gateway.domain.port.in.HandleDataPurgedUseCase;
import com.simon.fleet.gateway.domain.model.VehicleId;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Confirmación publicada por alerting-service cuando ya purgó las alertas en PostgreSQL. */
@Component
@RequiredArgsConstructor
public class VehicleDataPurgedConsumer {

    private final HandleDataPurgedUseCase handleDataPurgedUseCase;

    @RabbitListener(queues = RabbitMqConfig.DATA_PURGED_QUEUE)
    public void onDataPurged(VehicleDataPurgedEvent event) {
        handleDataPurgedUseCase.onDataPurged(new VehicleId(event.vehicleId()), event.purgedAt());
    }
}
