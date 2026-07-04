package com.simon.fleet.gateway.domain.model;

import java.time.Instant;

/**
 * Alerta ya levantada por alerting-service, tal como la conserva la vista de lectura del
 * dashboard. Es la copia propia de fleet-gateway-service de este concepto (no comparte tabla ni
 * clase con la de alerting-service): cada servicio guarda lo que necesita para su propio
 * propósito — aquí, poder listar el historial reciente sin depender de una consulta síncrona.
 */
public record Alert(String alertId, VehicleId vehicleId, String ruleCode, String message, Instant raisedAt) {
}
