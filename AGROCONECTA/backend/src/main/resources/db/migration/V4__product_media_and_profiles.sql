-- Medios de producto, características, disponibilidad y foto de perfil.
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(400);

ALTER TABLE products ADD COLUMN characteristics VARCHAR(400);
ALTER TABLE products ADD COLUMN sold_out BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(400) NOT NULL,
    content_type VARCHAR(60) NOT NULL,
    size_bytes BIGINT NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT ck_product_images_size CHECK (size_bytes > 0)
);

CREATE INDEX idx_product_images_product ON product_images (product_id, position);
