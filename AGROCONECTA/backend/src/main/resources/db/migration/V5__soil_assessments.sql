-- Cuestionario y resultados de análisis orientativo del suelo.
CREATE TABLE soil_assessments (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    parcel_id BIGINT,
    texture VARCHAR(30) NOT NULL,
    drainage VARCHAR(30) NOT NULL,
    color VARCHAR(30) NOT NULL,
    water_retention VARCHAR(30) NOT NULL,
    slope VARCHAR(30) NOT NULL,
    ph_level VARCHAR(30) NOT NULL,
    organic_matter VARCHAR(30) NOT NULL,
    compaction BOOLEAN NOT NULL,
    salinity BOOLEAN NOT NULL,
    erosion BOOLEAN NOT NULL,
    soil_type VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_soil_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_soil_parcel FOREIGN KEY (parcel_id) REFERENCES parcels (id) ON DELETE SET NULL
);

CREATE INDEX idx_soil_owner ON soil_assessments (owner_id, created_at DESC);
