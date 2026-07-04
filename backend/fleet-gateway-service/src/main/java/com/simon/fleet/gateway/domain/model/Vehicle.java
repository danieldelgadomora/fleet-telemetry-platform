package com.simon.fleet.gateway.domain.model;

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
 */
@Getter
public class Vehicle {

    private final VehicleId id;
    private VehicleStatus status;
    private final Instant registeredAt;
    private Instant cacheClearedAt;
    private Instant dataPurgedAt;
    private Double lastLat;
    private Double lastLng;
    private Instant lastReportedAt;
    private MovementStatus movementStatus;

    private Vehicle(VehicleId id, VehicleStatus status, Instant registeredAt,
                     Instant cacheClearedAt, Instant dataPurgedAt,
                     Double lastLat, Double lastLng, Instant lastReportedAt, MovementStatus movementStatus) {
        this.id = id;
        this.status = status;
        this.registeredAt = registeredAt;
        this.cacheClearedAt = cacheClearedAt;
        this.dataPurgedAt = dataPurgedAt;
        this.lastLat = lastLat;
        this.lastLng = lastLng;
        this.lastReportedAt = lastReportedAt;
        this.movementStatus = movementStatus;
    }

    public static Vehicle register(VehicleId id, Instant now) {
        return new Vehicle(id, VehicleStatus.ACTIVE, now, null, null, null, null, null, null);
    }

    /** Reconstruye un vehículo ya existente desde el almacén de persistencia. */
    public static Vehicle rehydrate(VehicleId id, VehicleStatus status, Instant registeredAt,
                                     Instant cacheClearedAt, Instant dataPurgedAt,
                                     Double lastLat, Double lastLng, Instant lastReportedAt,
                                     MovementStatus movementStatus) {
        return new Vehicle(id, status, registeredAt, cacheClearedAt, dataPurgedAt,
                lastLat, lastLng, lastReportedAt, movementStatus);
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
