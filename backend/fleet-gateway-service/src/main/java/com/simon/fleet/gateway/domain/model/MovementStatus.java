package com.simon.fleet.gateway.domain.model;

/**
 * Último estado conocido de un vehículo para el dashboard, derivado de los eventos de
 * telemetría y de alertas que publican ingestion-service y alerting-service:
 *
 * <ul>
 *   <li>{@code EN_MOVIMIENTO}: la última coordenada reportada es distinta de la anterior.</li>
 *   <li>{@code DETENIDO}: la coordenada no ha cambiado, pero todavía no se generó una alerta.</li>
 *   <li>{@code ALERTA}: alerting-service ya generó una alerta (ej. vehículo detenido por más
 *       de un minuto); se mantiene así hasta que el vehículo vuelva a moverse.</li>
 * </ul>
 */
public enum MovementStatus {
    EN_MOVIMIENTO,
    DETENIDO,
    ALERTA
}
