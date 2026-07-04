package com.simon.fleet.gateway.infrastructure.persistence.postgres;

import com.simon.fleet.gateway.domain.model.Alert;
import com.simon.fleet.gateway.domain.model.VehicleId;
import com.simon.fleet.gateway.domain.port.out.AlertRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Lee (nunca escribe) la tabla {@code alerts} que ya crea y mantiene alerting-service, en vez
 * de mantener una copia propia: es la misma Postgres física, y duplicar esos datos no aportaba
 * ninguna transformación real. Se usa {@code JdbcTemplate} en vez de una entidad JPA a
 * propósito: esta tabla no la migra ni la versiona fleet-gateway-service, así que no debe
 * participar de la validación de esquema ({@code ddl-auto: validate}) de este servicio como si
 * fuera dueño de su DDL.
 */
@Component
@RequiredArgsConstructor
public class JdbcAlertRepositoryAdapter implements AlertRepositoryPort {

    private static final String FIND_RECENT_SQL =
            "SELECT id, vehicle_id, rule_code, message, raised_at FROM alerts ORDER BY raised_at DESC LIMIT ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Alert> findRecent(int limit) {
        return jdbcTemplate.query(FIND_RECENT_SQL, (rs, rowNum) -> new Alert(
                rs.getString("id"),
                new VehicleId(rs.getString("vehicle_id")),
                rs.getString("rule_code"),
                rs.getString("message"),
                // La columna es `timestamp without time zone` pero guarda hora UTC (así la
                // escribe alerting-service): rs.getTimestamp(...).toInstant() interpretaría ese
                // valor naive en la zona horaria por defecto de la JVM, desplazando la hora real.
                // Leer como LocalDateTime e interpretarlo explícitamente como UTC evita esa
                // ambigüedad de zona horaria del driver JDBC.
                rs.getObject("raised_at", LocalDateTime.class).toInstant(ZoneOffset.UTC)
        ), limit);
    }
}
