# Modelo de datos

Base de datos: **PostgreSQL 16**. El esquema se crea mediante migraciones Flyway
(`backend/src/main/resources/db/migration`).

## Diagrama entidad-relación

```
users (1) ────< parcels (1) ────< crops (1) ────< agricultural_activities
  │                 │                  │
  │                 │                  └────< production_records
  │                 │
  │                 └────< expenses >────┐
  │                                       │
   └────< products (1) ────< product_images
   │          │
   │          └────< conversations (1) ────< messages
   └────< soil_assessments
   └────< processed_operations
   └────< expenses ────────────────────────┘
```

## Tablas

### `users`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK, identity |
| full_name | varchar(120) | not null |
| email | varchar(160) | not null, unique (sin distinguir mayúsculas) |
| password_hash | varchar(100) | not null |
| role | enum | `PRODUCTOR`, `COMPRADOR`, `ADMINISTRADOR` |
| phone | varchar(30) | nullable |
| location | varchar(120) | nullable |
| active | boolean | not null, default true |
| created_at / updated_at | timestamptz | not null |

### `parcels`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| owner_id | bigint | FK → users, not null |
| name | varchar(120) | not null |
| location | varchar(180) | not null |
| area | numeric(12,2) | not null (> 0) |
| description | varchar(500) | nullable |

### `crops`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| parcel_id | bigint | FK → parcels, not null |
| name | varchar(120) | not null |
| variety | varchar(120) | nullable |
| planting_date | date | not null |
| estimated_harvest_date | date | nullable |
| status | enum | `PLANIFICADO`, `EN_CRECIMIENTO`, `LISTO_PARA_COSECHA`, `COSECHADO`, `PERDIDO` |
| observations | varchar(500) | nullable |

### `agricultural_activities`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| crop_id | bigint | FK → crops, not null |
| type | enum | `SIEMBRA`, `RIEGO`, `FERTILIZACION`, `PODA`, `COSECHA`, `OTRO` |
| activity_date | date | not null |
| description | varchar(300) | not null |
| observations | varchar(500) | nullable |

### `expenses`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| owner_id | bigint | FK → users, not null |
| parcel_id | bigint | FK → parcels, nullable |
| crop_id | bigint | FK → crops, nullable |
| concept | varchar(180) | not null |
| amount | numeric(14,2) | not null (> 0) |
| expense_date | date | not null |

### `production_records`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| crop_id | bigint | FK → crops, not null |
| quantity | numeric(14,2) | not null (> 0) |
| unit | enum | `KILOGRAMO`, `TONELADA`, `LIBRA`, `BULTO`, `UNIDAD` |
| harvest_date | date | not null |
| observations | varchar(500) | nullable |

### `products`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| owner_id | bigint | FK → users, not null |
| name | varchar(150) | not null |
| description | varchar(600) | not null |
| characteristics | varchar(400) | nullable |
| category | enum | `FRUTAS`, `HORTALIZAS`, `GRANOS`, `TUBERCULOS`, `CAFE`, `OTROS` |
| price | numeric(14,2) | not null (> 0) |
| available_quantity | numeric(14,2) | not null (>= 0) |
| unit | enum | `KILOGRAMO`, `TONELADA`, `LIBRA`, `BULTO`, `UNIDAD` |
| contact_phone | varchar(40) | not null |
| contact_email | varchar(160) | not null |
| active | boolean | not null, default true |
| sold_out | boolean | not null, default false |

### `product_images`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| product_id | bigint | FK → products, not null |
| image_url | varchar(400) | not null |
| content_type | varchar(60) | not null |
| size_bytes | bigint | not null |
| position | integer | not null |

### `soil_assessments`
| Columna | Tipo | Restricciones |
| --- | --- | --- |
| id | bigint | PK |
| owner_id | bigint | FK → users, not null |
| parcel_id | bigint | FK → parcels, nullable |
| texture / drainage / color / water_retention / slope / ph_level / organic_matter | enum | not null |
| compaction / salinity / erosion | boolean | not null |
| soil_type | varchar(80) | not null |
| fertility_score | integer | not null |
| recommended_crops / discouraged_crops / crop_care / amendments / warnings | text | not null |
| created_at | timestamptz | not null |

### `conversations` y `messages`
`conversations` relaciona `buyer_id`, `producer_id` y un `product_id` opcional, con
`last_message_at`. `messages` guarda `conversation_id`, `sender_id`, `content` (máximo
2000 caracteres), `created_at` y `read_at`.

### `processed_operations`
Registra `owner_id`, `resource_type`, `resource_id` y `operation_key` para hacer
idempotentes las creaciones sincronizadas desde el cliente.

## Migraciones

| Versión | Archivo | Contenido |
| --- | --- | --- |
| V1 | `V1__init_schema.sql` | Creación de todas las tablas, índices y restricciones. |
| V2 | `V2__seed_demo_data.sql` | Datos de demostración (usuarios, parcelas, cultivos, productos, etc.). |
| V3 | `V3__demo_passwords.sql` | Contraseña conocida (`Agroconecta123`) para las cuentas demo. |
| V4 | `V4__product_media_and_profiles.sql` | Fotos de productos, avatares y características. |
| V5 | `V5__soil_assessments.sql` | Cuestionario de suelo y resultados. |
| V6 | `V6__chat.sql` | Conversaciones y mensajes. |
| V7 | `V7__sync_idempotency.sql` | Registro de operaciones procesadas. |
| V8 | `V8__soil_assessment_results.sql` | Resultados calculados del análisis de suelo. |
| V9 | `V9__soil_crop_guidance.sql` | Cultivos desaconsejados y cuidados por cultivo. |

## Índices relevantes

- `users(email)` único.
- `parcels(owner_id)`, `crops(parcel_id)`, `expenses(owner_id)`, `products(owner_id)`
  para acelerar los filtros por propietario.
- `products(active, category)` para el catálogo público.
- `product_images(product_id, position)`, `soil_assessments(owner_id, created_at)`,
  `conversations(buyer_id, producer_id, last_message_at)` y
  `messages(conversation_id, created_at)` para las consultas de galería, historial y chat.
- `processed_operations(owner_id, operation_key)` para controlar idempotencia.
