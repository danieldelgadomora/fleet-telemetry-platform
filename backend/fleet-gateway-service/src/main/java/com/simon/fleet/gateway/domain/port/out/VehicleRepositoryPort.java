package com.simon.fleet.gateway.domain.port.out;

import com.simon.fleet.gateway.domain.model.Vehicle;
import com.simon.fleet.gateway.domain.model.VehicleId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven) de persistencia del registro de vehículos (PostgreSQL). Las dos
 * confirmaciones de la Saga ({@code markCacheCleared} y {@code markDataPurged}) llegan de dos
 * consumers de RabbitMQ distintos que pueden ejecutarse casi al mismo tiempo; por eso son
 * actualizaciones SQL atómicas (columna suelta) en vez de "leer todo el vehículo, modificar en
 * Java, guardar todo el vehículo", que perdería una de las dos escrituras si llegan
 * concurrentes.
 */
public interface VehicleRepositoryPort {

    Optional<Vehicle> findById(VehicleId id);

    /** Usado solo para el registro inicial y para pedir el borrado (un único escritor cada vez). */
    void save(Vehicle vehicle);

    void markCacheCleared(VehicleId id, Instant when);

    void markDataPurged(VehicleId id, Instant when);

    /**
     * Transición atómica a {@code DELETED}, condicionada en SQL a que el vehículo esté
     * {@code PENDING_DELETION} y ambas confirmaciones ya estén registradas. Es seguro llamarla
     * repetidamente (idempotente): solo la primera vez que se cumple la condición produce un
     * cambio real; las demás no hacen nada.
     *
     * @return true si esta llamada fue la que efectivamente marcó el vehículo como DELETED.
     */
    boolean completeIfBothConfirmed(VehicleId id);

    /**
     * Registra el vehículo si todavía no existe (idempotente, vía upsert atómico). Lo usan los
     * consumers de telemetría/alertas cuando llega un evento de un vehículo que nunca pasó por
     * {@code POST /api/v1/vehicles}: el dashboard debe reflejar cualquier vehículo que esté
     * reportando, no solo los registrados explícitamente.
     */
    void registerIfAbsent(VehicleId id, Instant registeredAt);

    /**
     * Actualiza la última posición conocida y deriva el {@code movement_status} (EN_MOVIMIENTO
     * si la coordenada cambió, DETENIDO si no) en una única actualización SQL atómica, para que
     * dos lecturas casi simultáneas del mismo vehículo no se pisen entre sí. Si el vehículo ya
     * está en {@code ALERTA}, una coordenada repetida no lo regresa a DETENIDO: se necesita una
     * coordenada distinta (o sea, que el vehículo vuelva a moverse) para salir de ALERTA.
     *
     * @return true si el vehículo existía y se actualizó.
     */
    boolean updatePosition(VehicleId id, double lat, double lng, Instant when);

    /**
     * Marca el vehículo en {@code ALERTA}. Se queda así hasta que {@link #updatePosition}
     * reciba una coordenada distinta de la última conocida.
     *
     * @return true si el vehículo existía y se actualizó.
     */
    boolean markInAlert(VehicleId id);

    /** Vehículos {@code ACTIVE}, para el listado del dashboard. */
    List<Vehicle> findAllActive();
}
