package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Fila de la tabla {@code alerts}. Se evita {@code @Data} a propósito: generar
 * {@code equals}/{@code hashCode} automáticos sobre una entidad JPA puede comportarse mal con
 * los proxies de Hibernate (colecciones lazy, comparaciones antes de que el id esté asignado).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alerts")
public class AlertJpaEntity {

    @Id
    private String id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;
}
