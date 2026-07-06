package com.simon.fleet.alerting.infrastructure.persistence.postgres.repository;

import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.SafeZoneJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA de la tabla {@code safe_zones}. Spring Data genera la
 * implementación automáticamente por extender {@link JpaRepository}; no necesita
 * {@code @Repository} explícita. Se administra por SQL directo (ver README), no tiene API REST.
 */
public interface SafeZoneJpaRepository extends JpaRepository<SafeZoneJpaEntity, Long> {

    /** Zonas seguras activas, usadas por {@code SafeZoneAwareAlertRule} para suprimir alertas dentro de ellas. */
    List<SafeZoneJpaEntity> findByActiveTrue();
}
