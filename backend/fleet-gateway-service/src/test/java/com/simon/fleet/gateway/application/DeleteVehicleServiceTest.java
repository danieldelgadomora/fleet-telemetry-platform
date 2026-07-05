package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.InvalidVehicleStateException;
import com.simon.fleet.gateway.domain.exception.VehicleNotFoundException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.model.VehicleStatus;
import com.simon.fleet.gateway.domain.port.out.VehicleLifecycleEventPublisherPort;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteVehicleServiceTest {

    @Mock
    private VehicleRepositoryPort repositoryPort;
    @Mock
    private VehicleLifecycleEventPublisherPort eventPublisherPort;

    private final VehiclePlate plate = new VehiclePlate("ABC123");

    @Test
    @DisplayName("si el vehículo no existe, lanza VehicleNotFoundException sin guardar ni publicar")
    void vehiculoInexistenteLanzaExcepcionSinEfectosSecundarios() {
        when(repositoryPort.findById(plate)).thenReturn(Optional.empty());

        DeleteVehicleService service = new DeleteVehicleService(repositoryPort, eventPublisherPort);

        assertThatThrownBy(() -> service.requestDeletion(plate))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(repositoryPort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("un vehículo ACTIVE transiciona a PENDING_DELETION, se guarda y luego se publica el evento, en ese orden")
    void vehiculoActivoTransicionaGuardaYPublicaEnOrden() {
        Vehicle vehicle = Vehicle.register(plate, Instant.now());
        when(repositoryPort.findById(plate)).thenReturn(Optional.of(vehicle));

        DeleteVehicleService service = new DeleteVehicleService(repositoryPort, eventPublisherPort);

        Vehicle result = service.requestDeletion(plate);

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.PENDING_DELETION);

        InOrder order = inOrder(repositoryPort, eventPublisherPort);
        order.verify(repositoryPort).save(vehicle);
        order.verify(eventPublisherPort).publishDeletionRequested(plate);
    }

    @Test
    @DisplayName("un vehículo ya PENDING_DELETION propaga la excepción sin guardar ni publicar")
    void vehiculoYaPendingDeletionPropagaExcepcionSinEfectosSecundarios() {
        Vehicle vehicle = Vehicle.builder()
                .id(plate)
                .status(VehicleStatus.PENDING_DELETION)
                .registeredAt(Instant.now())
                .build();
        when(repositoryPort.findById(plate)).thenReturn(Optional.of(vehicle));

        DeleteVehicleService service = new DeleteVehicleService(repositoryPort, eventPublisherPort);

        assertThatThrownBy(() -> service.requestDeletion(plate))
                .isInstanceOf(InvalidVehicleStateException.class);

        verify(repositoryPort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }
}
