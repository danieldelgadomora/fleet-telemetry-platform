package com.simon.fleet.gateway.domain.model;

import com.simon.fleet.gateway.domain.exception.InvalidVehicleStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    private final VehiclePlate plate = new VehiclePlate("ABC123");
    private final Instant now = Instant.parse("2026-07-05T12:00:00Z");

    @Test
    @DisplayName("register() crea un vehículo en estado ACTIVE")
    void registerCreaVehiculoActivo() {
        Vehicle vehicle = Vehicle.register(plate, now);

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
        assertThat(vehicle.getRegisteredAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("requestDeletion() desde ACTIVE transiciona a PENDING_DELETION")
    void requestDeletionDesdeActiveTransicionaAPendingDeletion() {
        Vehicle vehicle = Vehicle.register(plate, now);

        vehicle.requestDeletion();

        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.PENDING_DELETION);
    }

    @Test
    @DisplayName("requestDeletion() desde PENDING_DELETION lanza InvalidVehicleStateException")
    void requestDeletionDesdePendingDeletionLanzaExcepcion() {
        Vehicle vehicle = Vehicle.builder()
                .id(plate)
                .status(VehicleStatus.PENDING_DELETION)
                .registeredAt(now)
                .build();

        assertThatThrownBy(vehicle::requestDeletion)
                .isInstanceOf(InvalidVehicleStateException.class)
                .hasMessageContaining("ABC123")
                .hasMessageContaining("PENDING_DELETION");
    }

    @Test
    @DisplayName("requestDeletion() desde DELETED lanza InvalidVehicleStateException")
    void requestDeletionDesdeDeletedLanzaExcepcion() {
        Vehicle vehicle = Vehicle.builder()
                .id(plate)
                .status(VehicleStatus.DELETED)
                .registeredAt(now)
                .cacheClearedAt(now)
                .dataPurgedAt(now)
                .build();

        assertThatThrownBy(vehicle::requestDeletion)
                .isInstanceOf(InvalidVehicleStateException.class)
                .hasMessageContaining("DELETED");
    }
}
