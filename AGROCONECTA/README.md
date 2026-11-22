> **Nota:** Este documento fue elaborado con apoyo de inteligencia artificial (IA), siguiendo mis indicaciones y revisiones, con el propósito de presentar de manera clara la información general, las tecnologías, la estructura y las instrucciones de uso de AgroConecta.

# AgroConecta

Plataforma web para la gestión agrícola de pequeños productores de la región Caribe
colombiana. Centraliza parcelas, cultivos, actividades, gastos y producción, y conecta a
los productores con compradores mediante un catálogo digital. También incluye
cuestionario de suelo, clima con Open-Meteo, chat entre usuarios, fotos de productos y
trabajo offline con sincronización posterior.

El proyecto está construido con una **arquitectura hexagonal (puertos y adaptadores)**,
aplicando principios **SOLID**, separación por capas y pruebas automatizadas.

---

## Tecnologías

| Capa        | Tecnología                                                        |
| ----------- | ----------------------------------------------------------------- |
| Backend     | Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA |
| Persistencia| PostgreSQL 16 · Flyway (migraciones)                              |
| Frontend    | React 18 · TypeScript · Vite · Tailwind CSS · React Router        |
| Pruebas     | JUnit 5 · Mockito · MockMvc (backend) · Vitest · Testing Library  |
| DevOps      | Docker · Docker Compose · Nginx                                   |

---

## Estructura del repositorio

```
AGROCONECTA/
├── backend/                 API REST Spring Boot (arquitectura hexagonal)
│   ├── src/main/java/com/agroconecta/
│   │   ├── domain/          Modelo de dominio, validaciones y excepciones
│   │   ├── application/     Casos de uso y puertos (in/out)
│   │   └── infrastructure/  Adaptadores web, seguridad y persistencia
│   └── src/main/resources/db/migration/   Migraciones Flyway (V1 a V9)
├── frontend/                SPA React + TypeScript
│   └── src/{pages,components,services,context,lib,utils,types}
├── docs/                    Documentación del proyecto
├── docker-compose.yml       Orquestación (PostgreSQL + backend + frontend)
└── .env.example             Variables de entorno de ejemplo
```

---

## Requisitos

- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+ y npm
- PostgreSQL 16 (o Docker)

---

## Ejecución local

### 1. Base de datos

Crea la base de datos y el usuario (o usa Docker: `docker compose up -d db`):

```sql
CREATE USER agroconecta WITH PASSWORD 'agroconecta';
CREATE DATABASE agroconecta OWNER agroconecta;
```

Flyway crea las tablas y carga los datos de demostración al arrancar el backend.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080` y la documentación Swagger en
`http://localhost:8080/swagger-ui.html`.

Variables opcionales (ver `.env.example`): `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`SERVER_PORT`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `UPLOAD_DIR`, `WEATHER_*`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173` y redirige las peticiones
`/api` y `/uploads` al backend mediante los proxies de Vite.

---

## Cuentas de demostración

| Rol            | Correo                    | Contraseña       |
| -------------- | ------------------------- | ---------------- |
| Administrador  | admin@agroconecta.co      | `Agroconecta123` |
| Productor      | maria@agroconecta.co      | `Agroconecta123` |
| Productor      | jose@agroconecta.co       | `Agroconecta123` |
| Comprador      | carlos@agroconecta.co     | `Agroconecta123` |

---

## API REST

Todas las respuestas usan el envoltorio `{ success, message, data, errors, timestamp }`.

| Método | Ruta                              | Acceso            | Descripción                         |
| ------ | --------------------------------- | ----------------- | ----------------------------------- |
| POST   | `/api/auth/register`              | Público           | Registrar productor o comprador     |
| POST   | `/api/auth/login`                 | Público           | Iniciar sesión (JWT)                |
| POST   | `/api/auth/logout`                | Autenticado       | Cerrar sesión                       |
| GET    | `/api/users/me`                   | Autenticado       | Perfil del usuario actual           |
| PUT    | `/api/users/me`                   | Autenticado       | Actualizar perfil                   |
| PATCH  | `/api/users/me/password`          | Autenticado       | Cambiar contraseña                  |
| GET    | `/api/users`                      | Administrador     | Listar usuarios                     |
| PATCH  | `/api/users/{id}/status`          | Administrador     | Activar / desactivar usuario        |
| PATCH  | `/api/users/{id}/role`            | Administrador     | Cambiar rol                         |
| GET    | `/api/parcels`                    | Productor         | Listar parcelas                     |
| POST   | `/api/parcels`                    | Productor         | Crear parcela                       |
| PUT    | `/api/parcels/{id}`               | Productor         | Actualizar parcela                  |
| DELETE | `/api/parcels/{id}`               | Productor         | Eliminar parcela                    |
| GET    | `/api/crops`                      | Productor         | Listar cultivos                     |
| POST   | `/api/crops`                      | Productor         | Crear cultivo                       |
| PATCH  | `/api/crops/{id}/status`          | Productor         | Cambiar estado del cultivo          |
| GET    | `/api/activities`                 | Productor         | Listar actividades agrícolas        |
| POST   | `/api/activities`                 | Productor         | Registrar actividad                 |
| GET    | `/api/expenses`                   | Productor         | Listar gastos                       |
| POST   | `/api/expenses`                   | Productor         | Registrar gasto                     |
| GET    | `/api/production`                 | Productor         | Listar registros de producción      |
| POST   | `/api/production`                 | Productor         | Registrar producción                |
| GET    | `/api/products`                   | Público           | Catálogo (búsqueda y categoría)     |
| GET    | `/api/products/mine`              | Productor         | Mis productos publicados            |
| POST   | `/api/products`                   | Productor         | Publicar producto                   |
| PATCH  | `/api/products/{id}/status`       | Productor         | Activar / desactivar producto       |
| GET    | `/api/products/{id}/images`       | Público           | Listar fotos del producto           |
| POST   | `/api/products/{id}/images`       | Productor         | Subir fotos (JPG/PNG/WEBP, 5 MB)    |
| DELETE | `/api/products/images/{id}`       | Productor         | Eliminar foto                       |
| DELETE | `/api/products/{id}`              | Productor         | Eliminar producto                   |
| POST   | `/api/soil/assessments`           | Productor         | Generar orientación de suelo        |
| GET    | `/api/soil/assessments`           | Productor         | Historial de orientaciones          |
| GET    | `/api/weather?location=`          | Autenticado       | Clima por ubicación                 |
| GET    | `/api/weather/parcels/{id}`       | Autenticado       | Clima por parcela y cultivos        |
| GET    | `/api/chat/conversations`         | Autenticado       | Listar conversaciones               |
| POST   | `/api/chat/conversations`         | Autenticado       | Iniciar conversación por producto   |
| GET    | `/api/chat/conversations/{id}/messages` | Autenticado | Listar mensajes                     |
| POST   | `/api/chat/conversations/{id}/messages` | Autenticado | Enviar mensaje                      |
| GET    | `/api/chat/unread-count`          | Autenticado       | Mensajes no leídos                  |
| GET    | `/api/dashboard/producer`         | Productor         | Indicadores del productor           |
| GET    | `/api/dashboard/buyer`            | Comprador         | Indicadores del comprador           |

Las rutas de productor (`parcels`, `crops`, `activities`, `expenses`, `production`)
responden `403` a usuarios con rol `COMPRADOR`.

---

## Pruebas

```bash
# Backend (53 pruebas)
cd backend && mvn test

# Frontend (38 pruebas)
cd frontend && npm run test
```

---

## Ejecución con Docker

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: `http://localhost:8081`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

---

## Documentación

La documentación detallada se encuentra en [`docs/`](docs/): arquitectura, modelo de
datos, manual de usuario, guía de API, guía de pruebas y guía de despliegue.

---

## Autor

Proyecto académico — *Diseñar Aplicación Moderna Orientada a la Web*.
