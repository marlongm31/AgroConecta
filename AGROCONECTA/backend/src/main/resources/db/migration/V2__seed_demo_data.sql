INSERT INTO users (id, full_name, email, password_hash, role, phone, location, active) VALUES
    (1, 'Administrador AgroConecta', 'admin@agroconecta.co', '$2a$12$NbVVAg8TBYWds6/MjFtWJO/vzfYvRLNSL.XUf5AMKnqhEDADfCHLa', 'ADMINISTRADOR', '+57 300 111 2233', 'Valledupar, Cesar', TRUE),
    (2, 'María Fernanda Gómez', 'maria@agroconecta.co', '$2a$12$8B7Zt1KHTwjav5PbfeXNauOV/I.7xvWRPqOSMhAPf/dRYkqobM3Ru', 'PRODUCTOR', '+57 312 456 7890', 'Valledupar, Cesar', TRUE),
    (3, 'Carlos Andrés Pérez', 'carlos@agroconecta.co', '$2a$12$mYa/84AK6fSrfMoeqicz6uoCI3ocx2iLOwFV9TzT0DCZKlRYhTuZa', 'COMPRADOR', '+57 305 987 6543', 'Barranquilla, Atlántico', TRUE),
    (4, 'José Luis Martínez', 'jose@agroconecta.co', '$2a$12$8B7Zt1KHTwjav5PbfeXNauOV/I.7xvWRPqOSMhAPf/dRYkqobM3Ru', 'PRODUCTOR', '+57 318 222 3344', 'Santa Marta, Magdalena', TRUE);

INSERT INTO parcels (id, owner_id, name, location, area, description) VALUES
    (1, 2, 'Finca La Esperanza', 'Vereda Los Venados, Valledupar', 3.50, 'Parcela principal con riego por goteo y cultivos de ciclo corto.'),
    (2, 2, 'Lote El Progreso', 'Corregimiento La Paz, Cesar', 1.80, 'Lote destinado a granos y rotación de cultivos.'),
    (3, 4, 'Finca Sierra Verde', 'Minca, Santa Marta', 5.00, 'Finca de ladera con café y frutales.');

INSERT INTO crops (id, parcel_id, name, variety, planting_date, estimated_harvest_date, status) VALUES
    (1, 1, 'Tomate', 'Chonto', DATE '2026-08-15', DATE '2026-12-10', 'EN_CRECIMIENTO'),
    (2, 1, 'Ají', 'Tabasco', DATE '2026-07-20', DATE '2026-11-30', 'LISTO_PARA_COSECHA'),
    (3, 2, 'Maíz', 'ICA V-305', DATE '2026-09-01', DATE '2027-01-15', 'EN_CRECIMIENTO'),
    (4, 3, 'Café', 'Castillo', DATE '2025-10-01', DATE '2026-09-30', 'LISTO_PARA_COSECHA'),
    (5, 3, 'Aguacate', 'Hass', DATE '2025-05-10', DATE '2027-02-28', 'EN_CRECIMIENTO');

INSERT INTO agricultural_activities (id, crop_id, type, activity_date, description, observations) VALUES
    (1, 1, 'RIEGO', DATE '2026-11-10', 'Riego por goteo de dos horas', 'Se verificó la humedad del suelo.'),
    (2, 2, 'FERTILIZACION', DATE '2026-11-08', 'Aplicación de fertilizante foliar', 'Dosis de 2 kg por hectárea.'),
    (3, 4, 'COSECHA', DATE '2026-11-05', 'Recolección selectiva de café maduro', 'Se recolectaron 1.200 kg.'),
    (4, 3, 'PODA', DATE '2026-11-02', 'Poda de formación', 'Se retiraron hojas secas.'),
    (5, 1, 'SIEMBRA', DATE '2026-08-15', 'Trasplante de plántulas al lote definitivo', 'Densidad de 30.000 plantas por hectárea.'),
    (6, 5, 'RIEGO', DATE '2026-11-12', 'Riego de mantenimiento', 'Riego profundo en la línea de árboles.');

INSERT INTO expenses (id, owner_id, parcel_id, crop_id, concept, amount, expense_date) VALUES
    (1, 2, 1, 1, 'Fertilizante NPK 15-15-15', 185000.00, DATE '2026-10-12'),
    (2, 2, 1, 2, 'Tutorado y amarre', 90000.00, DATE '2026-10-20'),
    (3, 2, 2, 3, 'Semilla certificada de maíz', 120000.00, DATE '2026-09-01'),
    (4, 4, 3, 4, 'Recolección de café', 320000.00, DATE '2026-11-05'),
    (5, 2, 1, NULL, 'Mantenimiento de cerca', 76000.00, DATE '2026-11-01');

INSERT INTO production_records (id, crop_id, quantity, unit, harvest_date, observations) VALUES
    (1, 2, 850.00, 'KILOGRAMO', DATE '2026-11-01', 'Cosecha escalonada de ají tabasco.'),
    (2, 4, 1200.00, 'KILOGRAMO', DATE '2026-11-05', 'Café pergamino seco.'),
    (3, 4, 15.00, 'BULTO', DATE '2026-11-06', 'Empaque para comercialización local.');

INSERT INTO products (id, owner_id, name, description, category, price, available_quantity, unit, contact_phone, contact_email, active, created_at) VALUES
    (1, 2, 'Tomate chonto fresco', 'Tomate chonto cultivado con manejo integrado de plagas, cosechado en la mañana y empacado el mismo día.', 'HORTALIZAS', 3500.00, 120.00, 'KILOGRAMO', '+57 312 456 7890', 'maria@agroconecta.co', TRUE, NOW() - INTERVAL '1 day'),
    (2, 2, 'Ají tabasco', 'Ají tabasco fresco de alta pungencia, ideal para salsas y preparaciones artesanales.', 'HORTALIZAS', 8000.00, 40.00, 'KILOGRAMO', '+57 312 456 7890', 'maria@agroconecta.co', TRUE, NOW() - INTERVAL '2 days'),
    (3, 4, 'Café Castillo pergamino', 'Café de altura cultivado en Minca, secado al sol y seleccionado manualmente.', 'CAFE', 18500.00, 300.00, 'KILOGRAMO', '+57 318 222 3344', 'jose@agroconecta.co', TRUE, NOW() - INTERVAL '3 days'),
    (4, 4, 'Aguacate Hass', 'Aguacate Hass de cosecha reciente, con excelente contenido de aceite y maduración uniforme.', 'FRUTAS', 6200.00, 180.00, 'KILOGRAMO', '+57 318 222 3344', 'jose@agroconecta.co', TRUE, NOW() - INTERVAL '4 days'),
    (5, 2, 'Maíz amarillo', 'Maíz amarillo para consumo animal y molienda artesanal.', 'GRANOS', 2400.00, 0.00, 'KILOGRAMO', '+57 312 456 7890', 'maria@agroconecta.co', FALSE, NOW() - INTERVAL '10 days'),
    (6, 4, 'Plátano hartón', 'Plátano hartón verde, cosechado en racimos y empacado para transporte.', 'HORTALIZAS', 2800.00, 90.00, 'KILOGRAMO', '+57 318 222 3344', 'jose@agroconecta.co', TRUE, NOW() - INTERVAL '5 days');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('parcels_id_seq', (SELECT MAX(id) FROM parcels));
SELECT setval('crops_id_seq', (SELECT MAX(id) FROM crops));
SELECT setval('agricultural_activities_id_seq', (SELECT MAX(id) FROM agricultural_activities));
SELECT setval('expenses_id_seq', (SELECT MAX(id) FROM expenses));
SELECT setval('production_records_id_seq', (SELECT MAX(id) FROM production_records));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
