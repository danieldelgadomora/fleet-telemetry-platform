package com.simon.fleet.alerting.infrastructure.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SafeZoneJpaRepository extends JpaRepository<SafeZoneJpaEntity, Long> {

    List<SafeZoneJpaEntity> findByActiveTrue();
}
