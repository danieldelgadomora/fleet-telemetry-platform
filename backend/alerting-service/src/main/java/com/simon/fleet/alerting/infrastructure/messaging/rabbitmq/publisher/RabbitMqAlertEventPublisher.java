package com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.publisher;

import com.simon.fleet.alerting.domain.model.Alert;
import com.simon.fleet.alerting.domain.model.VehiclePlate;
import com.simon.fleet.alerting.domain.port.out.AlertEventPublisherPort;
import com.simon.fleet.alerting.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import com.simon.fleet.contracts.alert.VehicleAlertRaisedEvent;
import com.simon.fleet.contracts.lifecycle.VehicleDataPurgedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RabbitMqAlertEventPublisher implements AlertEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishRaised(Alert alert) {
        VehicleAlertRaisedEvent event = new VehicleAlertRaisedEvent(
                alert.getAlertId(),
                alert.getPlate().value(),
                alert.getRuleCode(),
                alert.getMessage(),
                alert.getRaisedAt()
        );
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.FLEET_ALERTS_EXCHANGE, RabbitMqConfig.ALERT_RAISED_KEY, event);
    }

    @Override
    public void publishDataPurged(VehiclePlate plate) {
        VehicleDataPurgedEvent event = new VehicleDataPurgedEvent(plate.value(), Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.VEHICLE_LIFECYCLE_EXCHANGE, RabbitMqConfig.VEHICLE_DATA_PURGED_KEY, event);
    }
}
