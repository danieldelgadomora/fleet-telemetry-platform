package com.simon.fleet.gateway.infrastructure.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, String> {

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.cacheClearedAt = :when WHERE v.id = :id")
    void markCacheCleared(@Param("id") String id, @Param("when") Instant when);

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.dataPurgedAt = :when WHERE v.id = :id")
    void markDataPurged(@Param("id") String id, @Param("when") Instant when);

    /**
     * Atómica y condicionada en SQL: solo transiciona a DELETED si sigue PENDING_DELETION y
     * ambas confirmaciones ya llegaron. Si dos llamadas concurrentes la ejecutan, la primera
     * en ganar la fila hace el cambio real; la segunda actualiza 0 filas (la condición del
     * WHERE ya no se cumple).
     */
    @Modifying
    @Query("""
            UPDATE VehicleJpaEntity v SET v.status = 'DELETED'
            WHERE v.id = :id AND v.status = 'PENDING_DELETION'
              AND v.cacheClearedAt IS NOT NULL AND v.dataPurgedAt IS NOT NULL
            """)
    int completeIfBothConfirmed(@Param("id") String id);

    /**
     * Upsert atómico: si el vehículo ya existe, no hace nada (evita pisar su estado real con
     * uno "recién registrado" si dos eventos para el mismo vehículo nuevo llegan casi juntos).
     */
    @Modifying
    @Query(value = """
            INSERT INTO vehicles (id, status, registered_at)
            VALUES (:id, 'ACTIVE', :registeredAt)
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    void registerIfAbsent(@Param("id") String id, @Param("registeredAt") Instant registeredAt);

    /**
     * El CASE WHEN decide EN_MOVIMIENTO vs DETENIDO comparando con la posición ya guardada, y
     * nunca "degrada" un ALERTA existente por una coordenada repetida: solo una coordenada
     * distinta saca a un vehículo de ALERTA.
     */
    @Modifying
    @Query("""
            UPDATE VehicleJpaEntity v SET
                v.movementStatus = CASE
                    WHEN v.lastLat = :lat AND v.lastLng = :lng THEN
                        CASE WHEN v.movementStatus = 'ALERTA' THEN 'ALERTA' ELSE 'DETENIDO' END
                    ELSE 'EN_MOVIMIENTO'
                END,
                v.lastLat = :lat, v.lastLng = :lng, v.lastReportedAt = :when
            WHERE v.id = :id
            """)
    int updatePosition(@Param("id") String id, @Param("lat") double lat, @Param("lng") double lng,
                        @Param("when") Instant when);

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.movementStatus = 'ALERTA' WHERE v.id = :id")
    int markInAlert(@Param("id") String id);

    List<VehicleJpaEntity> findAllByStatus(String status);
}
