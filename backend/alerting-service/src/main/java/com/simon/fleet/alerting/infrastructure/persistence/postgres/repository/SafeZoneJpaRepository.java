package com.simon.fleet.alerting.infrastructure.persistence.postgres.repository;

import com.simon.fleet.alerting.infrastructure.persistence.postgres.entity.SafeZoneJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafeZoneJpaRepository extends JpaRepository<SafeZoneJpaEntity, Long> {

    List<SafeZoneJpaEntity> findByActiveTrue();
}
