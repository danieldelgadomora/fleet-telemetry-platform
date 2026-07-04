-- Vista de lectura denormalizada del estado de la flota: fleet-gateway-service la mantiene
-- suscribiendose a los eventos que ingestion-service (fleet.telemetry) y alerting-service
-- (fleet.alerts) ya publicaban, sin tocar sus bases de datos ni sus servicios.
ALTER TABLE vehicles
    ADD COLUMN last_lat          DOUBLE PRECISION,
    ADD COLUMN last_lng          DOUBLE PRECISION,
    ADD COLUMN last_reported_at  TIMESTAMP,
    ADD COLUMN movement_status   VARCHAR(32);
