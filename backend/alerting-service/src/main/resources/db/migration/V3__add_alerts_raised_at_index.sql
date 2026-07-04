-- fleet-gateway-service ahora lee esta tabla directamente para el historial reciente del
-- dashboard (ORDER BY raised_at DESC LIMIT): el indice que ya existia (vehicle_id, para la
-- purga de la Saga) no sirve para ese patron de acceso.
CREATE INDEX idx_alerts_raised_at ON alerts (raised_at DESC);
