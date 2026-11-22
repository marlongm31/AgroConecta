# Guía de la API REST

- **URL base:** `http://localhost:8080`
- **Documentación interactiva (Swagger UI):** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Formato:** JSON, codificación UTF-8.

## Autenticación

La API es sin estado. Tras iniciar sesión se obtiene un **JWT** que debe enviarse en la
cabecera `Authorization` de las peticiones protegidas:

```
Authorization: Bearer <token>
```

Las creaciones enviadas desde el modo offline pueden incluir
`Idempotency-Key: <uuid>` para que un reintento no duplique el recurso.

## Formato de respuesta

Todas las respuestas usan el mismo envoltorio:

```json
{
  "success": true,
  "message": "Operación realizada correctamente",
  "data": { },
  "errors": null,
  "timestamp": "2026-01-01T12:00:00Z"
}
```

Los listados paginados devuelven `data` con la forma:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

## Códigos de estado

| Código | Significado |
| --- | --- |
| 200 | Operación exitosa |
| 201 | Recurso creado |
| 400 | Datos de entrada inválidos (`errors` detalla los campos) |
| 401 | No autenticado o token inválido/expirado |
| 403 | Autenticado sin permisos para el recurso |
| 404 | Recurso no encontrado |
| 409 | Conflicto (p. ej. correo ya registrado) |
| 500 | Error interno del servidor |

---

## Autenticación (`/api/auth`)

### POST `/api/auth/register` — público
```json
{
  "fullName": "María Fernanda Gómez",
  "email": "maria@agroconecta.co",
  "password": "Agroconecta123",
  "role": "PRODUCTOR",
  "phone": "+57 312 456 7890",
  "location": "Valledupar, Cesar"
}
```
Respuesta `201` con `{ token, user }`.

### POST `/api/auth/login` — público
```json
{ "email": "maria@agroconecta.co", "password": "Agroconecta123" }
```
Respuesta `200` con `{ token, user }`.

### POST `/api/auth/logout` — autenticado
Invalida la sesión en el cliente. Respuesta `200`.

---

## Usuarios (`/api/users`)

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/users/me` | autenticado | Perfil actual |
| PUT | `/api/users/me` | autenticado | Actualiza `fullName`, `phone`, `location` |
| PATCH | `/api/users/me/password` | autenticado | Cambia contraseña |
| GET | `/api/users?page=0&size=10` | ADMINISTRADOR | Lista paginada |
| PATCH | `/api/users/{id}/status` | ADMINISTRADOR | `{ "active": false }` |
| PATCH | `/api/users/{id}/role` | ADMINISTRADOR | `{ "role": "COMPRADOR" }` |

---

## Parcelas (`/api/parcels`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/parcels` | Lista las parcelas del productor |
| GET | `/api/parcels/{id}` | Detalle |
| POST | `/api/parcels` | Crear |
| PUT | `/api/parcels/{id}` | Actualizar |
| DELETE | `/api/parcels/{id}` | Eliminar |

Cuerpo: `{ "name", "location", "area", "description" }`.

## Cultivos (`/api/crops`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/crops?parcelId=` | Lista (filtro opcional por parcela) |
| POST | `/api/crops` | Crear |
| PUT | `/api/crops/{id}` | Actualizar |
| PATCH | `/api/crops/{id}/status` | `{ "status": "EN_CRECIMIENTO" }` |
| DELETE | `/api/crops/{id}` | Eliminar |

## Actividades (`/api/activities`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/activities?cropId=` | Lista |
| POST | `/api/activities` | Crear |
| PUT | `/api/activities/{id}` | Actualizar |
| DELETE | `/api/activities/{id}` | Eliminar |

Cuerpo: `{ "cropId", "type", "activityDate", "description", "observations" }`.

## Gastos (`/api/expenses`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/expenses` | Lista |
| POST | `/api/expenses` | Crear |
| PUT | `/api/expenses/{id}` | Actualizar |
| DELETE | `/api/expenses/{id}` | Eliminar |

Cuerpo: `{ "parcelId", "cropId", "concept", "amount", "expenseDate" }`.

## Producción (`/api/production`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/production` | Lista |
| POST | `/api/production` | Crear |
| PUT | `/api/production/{id}` | Actualizar |
| DELETE | `/api/production/{id}` | Eliminar |

Cuerpo: `{ "cropId", "quantity", "unit", "harvestDate", "observations" }`.

## Productos (`/api/products`)

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/products?search=&category=&page=&size=` | público | Catálogo activo |
| GET | `/api/products/{id}` | público | Detalle |
| GET | `/api/products/mine` | PRODUCTOR | Mis publicaciones |
| POST | `/api/products` | PRODUCTOR | Publicar |
| PUT | `/api/products/{id}` | PRODUCTOR | Actualizar |
| PATCH | `/api/products/{id}/status` | PRODUCTOR | `{ "active": false }` |
| DELETE | `/api/products/{id}` | PRODUCTOR | Eliminar |

El campo `soldOut` de la respuesta se calcula automáticamente: es `true` cuando
`availableQuantity` es cero. El catálogo público solo devuelve productos activos
y con existencias.

### Fotos de productos (`/api/products`)

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/products/{id}/images` | público | Lista las fotos en orden de posición |
| POST | `/api/products/{id}/images` | PRODUCTOR | Sube `file` (JPG/PNG/WEBP, máximo 5 MB, máximo 6 fotos) |
| DELETE | `/api/products/images/{imageId}` | PRODUCTOR | Elimina una foto propia |

Las URLs devueltas empiezan por `/uploads/` y se sirven públicamente desde el backend.

## Suelo (`/api/soil/assessments`) — PRODUCTOR

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/soil/assessments` | Guarda una orientación calculada a partir del cuestionario |
| GET | `/api/soil/assessments?page=&size=` | Historial del productor |
| GET | `/api/soil/assessments/{id}` | Detalle de una orientación |

La respuesta incluye `soilType`, `fertilityScore`, `fertilityLabel`,
`recommendedCrops`, `discouragedCrops`, `cropCare`, `amendments`, `warnings` y
`disclaimer`.

## Clima (`/api/weather`) — autenticado

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/weather?location=` | Pronóstico por ciudad o municipio usando Open-Meteo |
| GET | `/api/weather/parcels/{parcelId}` | Pronóstico por parcela e indicación de cultivos en alertas |

La respuesta contiene `days`, `advisories`, `fetchedAt` y la ubicación resuelta.

## Chat (`/api/chat`) — autenticado

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/chat/conversations` | Inicia o recupera la conversación de un producto (`{ "productId" }`) |
| GET | `/api/chat/conversations?page=&size=` | Lista conversaciones del usuario |
| GET | `/api/chat/conversations/{id}` | Detalle de la conversación |
| GET | `/api/chat/conversations/{id}/messages?after=` | Mensajes; `after` filtra por identificador |
| POST | `/api/chat/conversations/{id}/messages` | Envía `{ "content" }` |
| GET | `/api/chat/unread-count` | Número de mensajes no leídos |

Solo el comprador o el productor participantes pueden leer o escribir en una
conversación.

## Dashboard (`/api/dashboard`)

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/api/dashboard/producer` | PRODUCTOR | Indicadores del productor |
| GET | `/api/dashboard/buyer` | COMPRADOR | Indicadores del comprador |

---

## Ejemplo con `curl`

```bash
# 1. Iniciar sesión
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@agroconecta.co","password":"Agroconecta123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2. Consultar el perfil
curl http://localhost:8080/api/users/me -H "Authorization: Bearer $TOKEN"

# 3. Ver el dashboard del productor
curl http://localhost:8080/api/dashboard/producer -H "Authorization: Bearer $TOKEN"
```
