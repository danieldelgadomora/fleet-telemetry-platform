package com.simon.fleet.alerting.infrastructure.persistence.postgres.repository;

import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.AlertJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la tabla {@code alerts}. Spring Data genera la implementación
 * automáticamente por extender {@link JpaRepository}; no necesita {@code @Repository} explícita.
 */
public interface AlertJpaRepository extends JpaRepository<AlertJpaEntity, String> {

    /** Borra el histórico de alertas de la placa (participante de la Saga de eliminación). */
    void deleteByPlate(String plate);
}
