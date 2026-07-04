package com.simon.fleet.gateway.domain.model;

/**
 * Ciclo de vida de un vehículo dentro de la Saga de eliminación:
 * {@code ACTIVE -> PENDING_DELETION -> DELETED}. No hay vuelta atrás desde
 * {@code PENDING_DELETION}: una vez se pide el borrado, se favorece recuperación hacia
 * adelante (forward recovery), no rollback.
 */
public enum VehicleStatus {
    ACTIVE,
    PENDING_DELETION,
    DELETED
}
