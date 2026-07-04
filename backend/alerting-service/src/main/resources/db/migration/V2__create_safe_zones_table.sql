CREATE TABLE safe_zones (
    id            BIGSERIAL        PRIMARY KEY,
    name          VARCHAR(120)     NOT NULL,
    center_lat    DOUBLE PRECISION NOT NULL,
    center_lng    DOUBLE PRECISION NOT NULL,
    radius_meters DOUBLE PRECISION NOT NULL,
    active        BOOLEAN          NOT NULL DEFAULT TRUE
);

-- Patron de acceso principal: SafeZoneAwareAlertRule solo consulta zonas activas.
CREATE INDEX idx_safe_zones_active ON safe_zones (active);
