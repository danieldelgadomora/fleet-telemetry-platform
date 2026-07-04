CREATE TABLE vehicles (
    id                VARCHAR(64)  NOT NULL PRIMARY KEY,
    status            VARCHAR(32)  NOT NULL,
    registered_at     TIMESTAMP    NOT NULL,
    cache_cleared_at  TIMESTAMP,
    data_purged_at    TIMESTAMP
);

CREATE INDEX idx_vehicles_status ON vehicles (status);
