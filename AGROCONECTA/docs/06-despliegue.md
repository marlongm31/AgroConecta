# Guía de despliegue

## 1. Requisitos

- **Con Docker:** Docker Engine 24+ y Docker Compose v2.
- **Sin Docker:** Java 21, Maven 3.9+, Node.js 20+, PostgreSQL 16.

---

## 2. Despliegue con Docker Compose (recomendado)

El archivo `docker-compose.yml` levanta tres servicios: PostgreSQL, el backend Spring
Boot y el frontend servido por Nginx.

```bash
# 1. Crear el archivo de variables de entorno
cp .env.example .env
# 2. Editar .env (al menos JWT_SECRET) y construir
docker compose up --build
```

Servicios expuestos:

| Servicio | URL |
| --- | --- |
| Frontend (Nginx) | http://localhost:8081 |
| Backend (API) | http://localhost:8080 |
| PostgreSQL | localhost:5432 |

Para detener:

```bash
docker compose down          # conserva los datos
docker compose down -v       # elimina tambien el volumen de datos
```

> El frontend (Nginx) redirige las peticiones `/api/` y `/uploads/` al backend dentro
> de la red de Docker, por lo que no se requiere configurar CORS para este escenario.
> Las fotos se conservan en el volumen `agroconecta-uploads`.

---

## 3. Despliegue manual

### 3.1 Base de datos

```sql
CREATE USER agroconecta WITH PASSWORD 'agroconecta';
CREATE DATABASE agroconecta OWNER agroconecta;
```

Flyway aplica automáticamente las migraciones `V1` a `V9` al arrancar el backend.

### 3.2 Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/agroconecta-backend-1.0.0.jar
```

Variables de entorno (con sus valores por defecto entre paréntesis):

| Variable | Descripción |
| --- | --- |
| `DB_URL` | URL JDBC (`jdbc:postgresql://localhost:5432/agroconecta`) |
| `DB_USER` | Usuario de la base de datos (`agroconecta`) |
| `DB_PASSWORD` | Contraseña de la base de datos (`agroconecta`) |
| `SERVER_PORT` | Puerto HTTP (`8080`) |
| `JWT_SECRET` | Secreto de firma de los JWT (cambiar en producción) |
| `JWT_EXPIRATION_MS` | Vigencia del token en milisegundos (`86400000`) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma |
| `UPLOAD_DIR` | Ruta local para guardar fotos (`uploads`) |
| `WEATHER_GEOCODING_URL` | Endpoint de geocodificación (`https://geocoding-api.open-meteo.com/v1/search`) |
| `WEATHER_FORECAST_URL` | Endpoint del pronóstico (`https://api.open-meteo.com/v1/forecast`) |
| `WEATHER_CACHE_TTL_SECONDS` | Vigencia de la caché climática (`900`) |
| `WEATHER_CONNECT_TIMEOUT_MS` / `WEATHER_READ_TIMEOUT_MS` | Timeouts del proveedor climático (`5000` / `8000`) |

### 3.3 Frontend

```bash
cd frontend
npm ci
npm run build            # genera la carpeta dist/
```

Sirve `dist/` con cualquier servidor estático. El `nginx.conf` incluido sirve la SPA y
actúa como proxy de `/api/` y `/uploads/`. Si el frontend y el backend están en dominios
distintos, define `VITE_API_URL` con la URL absoluta del backend antes de compilar.

---

## 4. Checklist de producción

- [ ] Cambiar `JWT_SECRET` por un valor largo y aleatorio.
- [ ] Usar contraseñas de base de datos robustas y no reutilizar las de demo.
- [ ] Restringir `CORS_ALLOWED_ORIGINS` a los dominios reales.
- [ ] Servir la aplicación detrás de HTTPS (terminación TLS en el proxy/balanceador).
- [ ] Eliminar o desactivar las cuentas de demostración.
- [ ] Configurar respaldos periódicos de PostgreSQL y del directorio/volumen de fotos.
- [ ] Verificar la conectividad saliente hacia Open-Meteo o ajustar los tiempos de espera.
- [ ] Ajustar el nivel de logs (`logging.level.*`) según el entorno.

---

## 5. Endpoints de salud

| Ruta | Descripción |
| --- | --- |
| `/actuator/health` | Estado de la aplicación |
| `/actuator/info` | Información de la aplicación |
| `/v3/api-docs` | Especificación OpenAPI |
| `/swagger-ui.html` | Documentación interactiva |
