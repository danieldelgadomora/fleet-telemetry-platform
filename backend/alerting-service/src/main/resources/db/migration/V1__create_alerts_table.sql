CREATE TABLE alerts (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    vehicle_id VARCHAR(64)  NOT NULL,
    rule_code  VARCHAR(64)  NOT NULL,
    message    TEXT         NOT NULL,
    raised_at  TIMESTAMP    NOT NULL
);

-- La Saga de borrado purga por vehiculo, y el dashboard futuro listara alertas por vehiculo:
-- este es el patron de acceso principal de la tabla.
CREATE INDEX idx_alerts_vehicle_id ON alerts (vehicle_id);
