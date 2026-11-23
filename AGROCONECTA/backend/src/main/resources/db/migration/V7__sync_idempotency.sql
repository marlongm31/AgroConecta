-- Claves de idempotencia para evitar duplicados al sincronizar registros offline.
CREATE TABLE processed_operations (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    operation_key VARCHAR(120) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_processed_operations_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_processed_operations_key UNIQUE (owner_id, operation_key)
);

CREATE INDEX idx_processed_operations_resource ON processed_operations (resource_type, resource_id);
