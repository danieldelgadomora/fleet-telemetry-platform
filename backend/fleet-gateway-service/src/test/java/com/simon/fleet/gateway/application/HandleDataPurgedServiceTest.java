package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.out.FleetStatusBroadcastPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Análogo a {@link HandleCacheClearedServiceTest}: junto con aquel, cubre las dos posibles
 * órdenes de llegada de las confirmaciones de la Saga. La condición de carrera real vive en SQL
 * (ver esa clase para el detalle del gap de cobertura).
 */
@ExtendWith(MockitoExtension.class)
class HandleDataPurgedServiceTest {

    @Mock
    private VehicleRepositoryPort repositoryPort;
    @Mock
    private FleetStatusBroadcastPort fleetStatusBroadcastPort;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Instant purgedAt = Instant.now();

    @Test
    @DisplayName("si esta confirmación completa la Saga, hace broadcast del estado final")
    void siCompletaLaSagaHaceBroadcast() {
        when(repositoryPort.completeIfBothConfirmed(plate)).thenReturn(true);
        Vehicle vehicle = Vehicle.register(plate, Instant.now());
        when(repositoryPort.findById(plate)).thenReturn(Optional.of(vehicle));

        HandleDataPurgedService service = new HandleDataPurgedService(repositoryPort, fleetStatusBroadcastPort);
        service.onDataPurged(plate, purgedAt);

        InOrder order = inOrder(repositoryPort);
        order.verify(repositoryPort).markDataPurged(plate, purgedAt);
        order.verify(repositoryPort).completeIfBothConfirmed(plate);
        verify(fleetStatusBroadcastPort).broadcastStatus(vehicle);
    }

    @Test
    @DisplayName("si todavía falta la otra confirmación, no hace broadcast")
    void siFaltaLaOtraConfirmacionNoHaceBroadcast() {
        when(repositoryPort.completeIfBothConfirmed(plate)).thenReturn(false);

        HandleDataPurgedService service = new HandleDataPurgedService(repositoryPort, fleetStatusBroadcastPort);
        service.onDataPurged(plate, purgedAt);

        verify(repositoryPort).markDataPurged(plate, purgedAt);
        verify(repositoryPort, never()).findById(plate);
        verify(fleetStatusBroadcastPort, never()).broadcastStatus(any());
    }
}
