CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(30),
    location VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('PRODUCTOR', 'COMPRADOR', 'ADMINISTRADOR'))
);

CREATE TABLE parcels (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    location VARCHAR(180) NOT NULL,
    area NUMERIC(12, 2) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parcels_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_parcels_area CHECK (area > 0)
);

CREATE TABLE crops (
    id BIGSERIAL PRIMARY KEY,
    parcel_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    variety VARCHAR(120),
    planting_date DATE NOT NULL,
    estimated_harvest_date DATE,
    status VARCHAR(30) NOT NULL,
    observations VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_crops_parcel FOREIGN KEY (parcel_id) REFERENCES parcels (id) ON DELETE CASCADE,
    CONSTRAINT ck_crops_status CHECK (status IN ('PLANIFICADO', 'EN_CRECIMIENTO', 'LISTO_PARA_COSECHA', 'COSECHADO', 'PERDIDO')),
    CONSTRAINT ck_crops_dates CHECK (estimated_harvest_date IS NULL OR estimated_harvest_date >= planting_date)
);

CREATE TABLE agricultural_activities (
    id BIGSERIAL PRIMARY KEY,
    crop_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    activity_date DATE NOT NULL,
    description VARCHAR(300) NOT NULL,
    observations VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activities_crop FOREIGN KEY (crop_id) REFERENCES crops (id) ON DELETE CASCADE,
    CONSTRAINT ck_activities_type CHECK (type IN ('SIEMBRA', 'RIEGO', 'FERTILIZACION', 'PODA', 'COSECHA', 'OTRO'))
);

CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    parcel_id BIGINT,
    crop_id BIGINT,
    concept VARCHAR(180) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    expense_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_parcel FOREIGN KEY (parcel_id) REFERENCES parcels (id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_crop FOREIGN KEY (crop_id) REFERENCES crops (id) ON DELETE CASCADE,
    CONSTRAINT ck_expenses_amount CHECK (amount > 0),
    CONSTRAINT ck_expenses_relation CHECK (parcel_id IS NOT NULL OR crop_id IS NOT NULL)
);

CREATE TABLE production_records (
    id BIGSERIAL PRIMARY KEY,
    crop_id BIGINT NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    harvest_date DATE NOT NULL,
    observations VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_production_crop FOREIGN KEY (crop_id) REFERENCES crops (id) ON DELETE CASCADE,
    CONSTRAINT ck_production_quantity CHECK (quantity > 0),
    CONSTRAINT ck_production_unit CHECK (unit IN ('KILOGRAMO', 'TONELADA', 'LIBRA', 'BULTO', 'UNIDAD'))
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(600) NOT NULL,
    category VARCHAR(30) NOT NULL,
    price NUMERIC(14, 2) NOT NULL,
    available_quantity NUMERIC(14, 2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    contact_phone VARCHAR(40) NOT NULL,
    contact_email VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_products_category CHECK (category IN ('FRUTAS', 'HORTALIZAS', 'GRANOS', 'TUBERCULOS', 'CAFE', 'OTROS')),
    CONSTRAINT ck_products_price CHECK (price > 0),
    CONSTRAINT ck_products_quantity CHECK (available_quantity >= 0),
    CONSTRAINT ck_products_unit CHECK (unit IN ('KILOGRAMO', 'TONELADA', 'LIBRA', 'BULTO', 'UNIDAD'))
);

CREATE INDEX idx_parcels_owner ON parcels (owner_id);
CREATE INDEX idx_crops_parcel ON crops (parcel_id);
CREATE INDEX idx_crops_status ON crops (status);
CREATE INDEX idx_activities_crop ON agricultural_activities (crop_id);
CREATE INDEX idx_activities_date ON agricultural_activities (activity_date);
CREATE INDEX idx_expenses_owner ON expenses (owner_id);
CREATE INDEX idx_expenses_parcel ON expenses (parcel_id);
CREATE INDEX idx_expenses_crop ON expenses (crop_id);
CREATE INDEX idx_production_crop ON production_records (crop_id);
CREATE INDEX idx_products_owner ON products (owner_id);
CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_name ON products (name);
