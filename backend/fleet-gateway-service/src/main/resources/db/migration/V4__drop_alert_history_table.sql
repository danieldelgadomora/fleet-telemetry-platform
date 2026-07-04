-- Revierte V3: fleet-gateway-service dejo de mantener su propia copia del historial de
-- alertas. Ahora lee directamente la tabla "alerts" de alerting-service (misma Postgres
-- fisica), evitando duplicar datos que no reciben ninguna transformacion real.
DROP TABLE alert_history;
