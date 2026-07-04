package com.simon.fleet.gateway.infrastructure.persistence.postgres.entity;

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
 * Fila de la tabla {@code vehicles}. Sin {@code @Data} a propósito (ver nota en
 * {@code AlertJpaEntity} de alerting-service): evita generar {@code equals}/{@code hashCode}
 * problemáticos sobre una entidad JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String status;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "cache_cleared_at")
    private Instant cacheClearedAt;

    @Column(name = "data_purged_at")
    private Instant dataPurgedAt;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @Column(name = "last_reported_at")
    private Instant lastReportedAt;

    @Column(name = "movement_status")
    private String movementStatus;
}
