package com.simon.fleet.gateway.domain.model;

import com.simon.fleet.gateway.domain.exception.InvalidVehicleStateException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate raíz del ciclo de vida de un vehículo. Es el "dueño" de la decisión de negocio de
 * cuándo un vehículo puede pasar de {@code ACTIVE} a {@code PENDING_DELETION}, dejando la
 * mecánica de concurrencia (cómo evitar una condición de carrera entre las dos confirmaciones
 * de la Saga, o entre telemetría y alertas llegando casi al mismo tiempo) al adaptador de
 * persistencia, que la resuelve con actualizaciones SQL atómicas (ver
 * {@code VehicleRepositoryPort#completeIfBothConfirmed} y {@code #updatePosition}).
 *
 * <p>Además del estado de la Saga, guarda el último estado conocido para el dashboard
 * ({@link MovementStatus}, última posición): es una vista de lectura que fleet-gateway-service
 * mantiene suscribiéndose a los eventos de ingestion-service y alerting-service.
 *
 * <p>Se construye con el Builder Pattern (vía Lombok), igual que {@code Alert} en
 * alerting-service: tiene varios campos opcionales (los de la Saga y los de última posición
 * empiezan en {@code null}) y un builder es más legible que un constructor con muchos
 * parámetros posicionales.
 */
@Getter
@Builder
public class Vehicle {

    private final VehiclePlate id;
    private VehicleStatus status;
    private final Instant registeredAt;
    private Instant cacheClearedAt;
    private Instant dataPurgedAt;
    private Double lastLat;
    private Double lastLng;
    private Instant lastReportedAt;
    private MovementStatus movementStatus;

    /** Crea un vehículo nuevo en estado {@code ACTIVE}, sin ningún dato de la Saga ni de posición. */
    public static Vehicle register(VehiclePlate id, Instant now) {
        return Vehicle.builder()
                .id(id)
                .status(VehicleStatus.ACTIVE)
                .registeredAt(now)
                .build();
    }

    /**
     * Arranca la Saga de eliminación. Solo un vehículo {@code ACTIVE} puede pedir borrado: no
     * tiene sentido volver a pedirlo si ya está en curso o ya se borró.
     *
     * @throws InvalidVehicleStateException si el vehículo no está en estado {@code ACTIVE}.
     */
    public void requestDeletion() {
        if (status != VehicleStatus.ACTIVE) {
            throw new InvalidVehicleStateException(
                    "El vehículo %s no está ACTIVE (estado actual: %s), no se puede pedir su borrado"
                            .formatted(id.value(), status));
        }
        this.status = VehicleStatus.PENDING_DELETION;
    }
}
