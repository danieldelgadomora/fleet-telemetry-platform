package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fila de la tabla {@code safe_zones}. Se evita {@code @Data} a propósito: generar
 * {@code equals}/{@code hashCode} automáticos sobre una entidad JPA puede comportarse mal con
 * los proxies de Hibernate (colecciones lazy, comparaciones antes de que el id esté asignado).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "safe_zones")
public class SafeZoneJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lng", nullable = false)
    private double centerLng;

    @Column(name = "radius_meters", nullable = false)
    private double radiusMeters;

    @Column(name = "active", nullable = false)
    private boolean active;
}
