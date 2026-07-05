package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleAlreadyRegisteredException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterVehicleServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-05T12:00:00Z");

    @Mock
    private VehicleRepositoryPort repositoryPort;

    @Captor
    private ArgumentCaptor<Vehicle> vehicleCaptor;

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    @DisplayName("si la placa ya está registrada, lanza VehicleAlreadyRegisteredException sin guardar")
    void placaYaRegistradaLanzaExcepcionSinGuardar() {
        when(repositoryPort.findById(plate)).thenReturn(Optional.of(Vehicle.register(plate, NOW)));

        RegisterVehicleService service = new RegisterVehicleService(repositoryPort, fixedClock);

        assertThatThrownBy(() -> service.register(plate))
                .isInstanceOf(VehicleAlreadyRegisteredException.class);

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("una placa nueva se registra como ACTIVE con la hora del reloj inyectado")
    void placaNuevaSeRegistraComoActiva() {
        when(repositoryPort.findById(plate)).thenReturn(Optional.empty());

        RegisterVehicleService service = new RegisterVehicleService(repositoryPort, fixedClock);
        Vehicle result = service.register(plate);

        assertThat(result.getRegisteredAt()).isEqualTo(NOW);
        verify(repositoryPort).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getId()).isEqualTo(plate);
    }
}
