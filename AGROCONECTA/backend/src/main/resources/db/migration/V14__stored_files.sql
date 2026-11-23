-- Almacena los archivos subidos (fotos de productos, avatares, adjuntos de chat)
-- dentro de la base de datos, para que no se pierdan cuando el servidor se reinicia.
CREATE TABLE stored_files (
    public_url VARCHAR(200) PRIMARY KEY,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
