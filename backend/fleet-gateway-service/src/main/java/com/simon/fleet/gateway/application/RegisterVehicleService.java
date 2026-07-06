package com.simon.fleet.gateway.application;

import com.simon.fleet.gateway.domain.exception.VehicleAlreadyRegisteredException;
import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehiclePlate;
import com.simon.fleet.gateway.domain.model.VehicleStatus;
import com.simon.fleet.gateway.domain.port.in.RegisterVehicleUseCase;
import com.simon.fleet.gateway.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/** Alta explícita de un vehículo (fuera del auto-registro por telemetría/alerta). */
@Service
@RequiredArgsConstructor
public class RegisterVehicleService implements RegisterVehicleUseCase {

    private final VehicleRepositoryPort repositoryPort;
    private final Clock clock;

    /**
     * Rechaza la placa si ya existe y sigue {@code ACTIVE}/{@code PENDING_DELETION}; si existe
     * pero está {@code DELETED} la reactiva; si no existe, la registra como {@code ACTIVE} nueva.
     */
    @Override
    public Vehicle register(VehiclePlate plate) {
        Optional<Vehicle> existing = repositoryPort.findById(plate);
        if (existing.isPresent() && existing.get().getStatus() != VehicleStatus.DELETED) {
            throw new VehicleAlreadyRegisteredException(plate);
        }

        Instant now = Instant.now(clock);
        if (existing.isPresent()) {
            // Placa DELETED que se vuelve a registrar explícitamente: se reactiva igual que si
            // hubiera vuelto a reportar telemetría, sin arrastrar datos de su ciclo de vida
            // anterior (ver VehicleRepositoryPort#registerOrReactivate).
            repositoryPort.registerOrReactivate(plate, now);
            return repositoryPort.findById(plate)
                    .orElseThrow(() -> new IllegalStateException(
                            "El vehículo %s debería existir justo después de reactivarse".formatted(plate.value())));
        }

        Vehicle vehicle = Vehicle.register(plate, now);
        repositoryPort.save(vehicle);
        return vehicle;
    }
}
