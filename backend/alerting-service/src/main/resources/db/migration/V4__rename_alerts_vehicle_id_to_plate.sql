ALTER TABLE alerts RENAME COLUMN vehicle_id TO plate;
ALTER INDEX idx_alerts_vehicle_id RENAME TO idx_alerts_plate;
