package com.simon.fleet.gateway.infrastructure.web.websocket;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.out.AlertBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.infrastructure.web.dto.VehicleResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Único adaptador STOMP del gateway: implementa ambos ports de broadcast porque comparten la
 * misma tecnología de salida ({@link SimpMessagingTemplate}), aunque sirvan canales distintos
 * ({@code /topic/fleet} para el estado agregado, {@code /topic/alerts} para notificaciones
 * puntuales). El envío es *best effort*: si falla (ej. no hay clientes suscritos, o uno se
 * desconectó a mitad de un envío), se registra y se descarta, sin propagar la excepción — el
 * estado ya quedó persistido correctamente antes de llegar aquí, así que un fallo de push nunca
 * debe hacer que el {@code @RabbitListener} que lo disparó reintente el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompDashboardBroadcaster implements FleetStatusBroadcastPort, AlertBroadcastPort {

    static final String FLEET_STATUS_TOPIC = "/topic/fleet";
    static final String ALERTS_TOPIC = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastStatus(Vehicle vehicle) {
        send(FLEET_STATUS_TOPIC, toVehicleResponseDto(vehicle));
    }

    @Override
    public void broadcastAlert(VehiclePlate plate, String alertId, String ruleCode, String message, Instant raisedAt) {
        send(ALERTS_TOPIC, new AlertMessage(alertId, plate.value(), ruleCode, message, raisedAt));
    }

    private void send(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el mensaje STOMP a {}: {}", destination, ex.getMessage());
        }
    }

    private static VehicleResponseDto toVehicleResponseDto(Vehicle vehicle) {
        return new VehicleResponseDto(
                vehicle.getId().value(),
                vehicle.getStatus().name(),
                vehicle.getRegisteredAt(),
                vehicle.getCacheClearedAt(),
                vehicle.getDataPurgedAt(),
                vehicle.getLastLat(),
                vehicle.getLastLng(),
                vehicle.getLastReportedAt(),
                vehicle.getMovementStatus() == null ? null : vehicle.getMovementStatus().name()
        );
    }
}
