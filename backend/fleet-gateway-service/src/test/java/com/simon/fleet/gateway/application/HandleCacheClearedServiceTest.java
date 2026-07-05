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
 * Cubre solo la orquestación de {@link HandleCacheClearedService} simulando ambos resultados
 * posibles de {@code completeIfBothConfirmed} — la condición de carrera real (el {@code WHERE}
 * SQL condicionado en {@code VehicleJpaRepository}) queda fuera de alcance de un test unitario
 * puro; requeriría un test de integración con Testcontainers.
 */
@ExtendWith(MockitoExtension.class)
class HandleCacheClearedServiceTest {

    @Mock
    private VehicleRepositoryPort repositoryPort;
    @Mock
    private FleetStatusBroadcastPort fleetStatusBroadcastPort;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Instant clearedAt = Instant.now();

    @Test
    @DisplayName("si esta confirmación completa la Saga, hace broadcast del estado final")
    void siCompletaLaSagaHaceBroadcast() {
        when(repositoryPort.completeIfBothConfirmed(plate)).thenReturn(true);
        Vehicle vehicle = Vehicle.register(plate, Instant.now());
        when(repositoryPort.findById(plate)).thenReturn(Optional.of(vehicle));

        HandleCacheClearedService service = new HandleCacheClearedService(repositoryPort, fleetStatusBroadcastPort);
        service.onCacheCleared(plate, clearedAt);

        InOrder order = inOrder(repositoryPort);
        order.verify(repositoryPort).markCacheCleared(plate, clearedAt);
        order.verify(repositoryPort).completeIfBothConfirmed(plate);
        verify(fleetStatusBroadcastPort).broadcastStatus(vehicle);
    }

    @Test
    @DisplayName("si todavía falta la otra confirmación, no hace broadcast")
    void siFaltaLaOtraConfirmacionNoHaceBroadcast() {
        when(repositoryPort.completeIfBothConfirmed(plate)).thenReturn(false);

        HandleCacheClearedService service = new HandleCacheClearedService(repositoryPort, fleetStatusBroadcastPort);
        service.onCacheCleared(plate, clearedAt);

        verify(repositoryPort).markCacheCleared(plate, clearedAt);
        verify(repositoryPort, never()).findById(plate);
        verify(fleetStatusBroadcastPort, never()).broadcastStatus(any());
    }
}
