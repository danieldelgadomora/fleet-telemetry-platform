package com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.consumer;

import com.simon.fleet.contracts.alert.VehicleAlertRaisedEvent;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.in.BroadcastAlertUseCase;
import com.simon.fleet.gateway.domain.port.in.HandleVehicleAlertRaisedUseCase;
import com.simon.fleet.gateway.infrastructure.messaging.rabbitmq.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de {@code fleet.alerts}: actualiza la vista de lectura del dashboard a ALERTA y
 * reenvía la alerta en tiempo real por WebSocket/STOMP. Primero se actualiza el estado (empuja
 * {@code /topic/fleet}) y luego se notifica la alerta puntual ({@code /topic/alerts}), para que
 * un cliente que reaccione a la notificación consultando el estado agregado ya lo encuentre
 * consistente. El historial de la alerta ya queda persistido por alerting-service en su propia
 * tabla {@code alerts}; el dashboard lo consulta directamente desde ahí (ver
 * {@code JdbcAlertRepositoryAdapter}), así que este consumer no necesita guardar nada más.
 */
@Component
@RequiredArgsConstructor
public class FleetStatusAlertConsumer {

    private final HandleVehicleAlertRaisedUseCase handleVehicleAlertRaisedUseCase;
    private final BroadcastAlertUseCase broadcastAlertUseCase;

    @RabbitListener(queues = RabbitMqConfig.FLEET_STATUS_ALERT_QUEUE)
    public void onAlertRaised(VehicleAlertRaisedEvent event) {
        VehicleId vehicleId = new VehicleId(event.vehicleId());
        handleVehicleAlertRaisedUseCase.onAlertRaised(vehicleId, event.raisedAt());
        broadcastAlertUseCase.broadcastAlert(
                vehicleId, event.alertId(), event.ruleCode(), event.message(), event.raisedAt());
    }
}
