package com.simon.fleet.gateway.infrastructure.persistence.postgres.repository;

import com.simon.fleet.gateway.infrastructure.persistence.postgres.entity.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, String> {

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.cacheClearedAt = :when WHERE v.plate = :plate")
    void markCacheCleared(@Param("plate") String plate, @Param("when") Instant when);

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.dataPurgedAt = :when WHERE v.plate = :plate")
    void markDataPurged(@Param("plate") String plate, @Param("when") Instant when);

    /**
     * Atómica y condicionada en SQL: solo transiciona a DELETED si sigue PENDING_DELETION y
     * ambas confirmaciones ya llegaron. Si dos llamadas concurrentes la ejecutan, la primera
     * en ganar la fila hace el cambio real; la segunda actualiza 0 filas (la condición del
     * WHERE ya no se cumple).
     */
    @Modifying
    @Query("""
            UPDATE VehicleJpaEntity v SET v.status = 'DELETED'
            WHERE v.plate = :plate AND v.status = 'PENDING_DELETION'
              AND v.cacheClearedAt IS NOT NULL AND v.dataPurgedAt IS NOT NULL
            """)
    int completeIfBothConfirmed(@Param("plate") String plate);

    /**
     * Upsert atómico: si la placa no existe, la registra como {@code ACTIVE}. Si ya existe pero
     * está {@code DELETED} (soft-delete: la fila nunca se borra físicamente), la reactiva como si
     * fuera un vehículo nuevo — mismo criterio que el alta automática: el dashboard debe reflejar
     * cualquier vehículo que esté reportando de verdad. Se resetean los campos de la Saga de
     * borrado anterior y la última posición conocida, para que arranque tan "limpio" como un
     * vehículo genuinamente nuevo. Si la placa ya está {@code ACTIVE}/{@code PENDING_DELETION},
     * el {@code WHERE} del `DO UPDATE` no aplica y el insert queda como no-op (no debería llegar
     * aquí de todas formas, porque {@code updatePosition}/{@code markInAlert} ya habrían tenido
     * éxito antes).
     */
    @Modifying
    @Query(value = """
            INSERT INTO vehicles (plate, status, registered_at)
            VALUES (:plate, 'ACTIVE', :registeredAt)
            ON CONFLICT (plate) DO UPDATE SET
                status = 'ACTIVE',
                registered_at = EXCLUDED.registered_at,
                cache_cleared_at = NULL,
                data_purged_at = NULL,
                last_lat = NULL,
                last_lng = NULL,
                last_reported_at = NULL,
                movement_status = NULL
            WHERE vehicles.status = 'DELETED'
            """, nativeQuery = true)
    void registerOrReactivate(@Param("plate") String plate, @Param("registeredAt") Instant registeredAt);

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
            WHERE v.plate = :plate AND v.status != 'DELETED'
            """)
    int updatePosition(@Param("plate") String plate, @Param("lat") double lat, @Param("lng") double lng,
                        @Param("when") Instant when);

    @Modifying
    @Query("UPDATE VehicleJpaEntity v SET v.movementStatus = 'ALERTA' WHERE v.plate = :plate AND v.status != 'DELETED'")
    int markInAlert(@Param("plate") String plate);

    List<VehicleJpaEntity> findAllByStatus(String status);
}
