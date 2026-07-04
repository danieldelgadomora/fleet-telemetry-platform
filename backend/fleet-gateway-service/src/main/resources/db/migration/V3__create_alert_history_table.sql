-- Copia propia de fleet-gateway-service del historial de alertas (separada de la tabla
-- "alerts" de alerting-service): esta vista de lectura existe solo para que el dashboard
-- liste alertas recientes sin depender de una consulta sincrona a otro servicio.
CREATE TABLE alert_history (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    vehicle_id VARCHAR(64) NOT NULL,
    rule_code  VARCHAR(64) NOT NULL,
    message    TEXT        NOT NULL,
    raised_at  TIMESTAMP   NOT NULL
);

-- Patron de acceso principal: listar las N mas recientes de toda la flota.
CREATE INDEX idx_alert_history_raised_at ON alert_history (raised_at DESC);
