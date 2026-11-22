> **Nota:** Esta guía fue elaborada con apoyo de inteligencia artificial (IA), siguiendo mis indicaciones y revisiones, con el propósito de explicar de manera clara cómo descargar, configurar, ejecutar y probar el proyecto AgroConecta.

# Guía para el profesor — AgroConecta

## 1. Introducción y objetivo de AgroConecta

AgroConecta es una plataforma web para la gestión agrícola de pequeños productores de la región Caribe colombiana. Su objetivo es centralizar la información de parcelas, cultivos, actividades agrícolas, gastos y producción, y conectar a los productores con compradores mediante un catálogo digital de productos.

El proyecto implementa una API REST con arquitectura hexagonal (puertos y adaptadores) y una aplicación web (SPA) que consume dicha API.

## 2. Módulos y funcionalidades

| Módulo | Acceso | Funcionalidades principales |
| --- | --- | --- |
| Autenticación | Público | Registro (roles Productor o Comprador), inicio de sesión (JWT) y cierre de sesión |
| Perfil | Autenticado | Ver y actualizar datos (nombre, teléfono, ubicación) y cambiar contraseña |
| Dashboard | Autenticado (por rol) | Indicadores: productor (parcelas, cultivos activos, gastos, producción, productos, últimas actividades) y comprador (productos disponibles y publicaciones recientes) |
| Parcelas | Productor | Crear, listar, ver, actualizar y eliminar parcelas |
| Cultivos | Productor | Gestionar cultivos por parcela y cambiar estado (planificado, en crecimiento, listo para cosecha, cosechado, perdido) |
| Actividades agrícolas | Productor | Registrar labores (siembra, riego, fertilización, poda, cosecha u otras) por cultivo |
| Gastos | Productor | Registrar y gestionar gastos asociados a parcelas/cultivos |
| Producción | Productor | Registrar cosechas por cultivo (cantidad y unidad) |
| Productos | Público / Productor | Catálogo público con búsqueda y filtro por categoría; el productor publica, edita, activa/desactiva y elimina sus productos |
| Administración de usuarios | Administrador | Listar usuarios, activar/desactivar cuentas y cambiar roles |

**Control de acceso:** las rutas de productor (`/api/parcels`, `/api/crops`, `/api/activities`, `/api/expenses`, `/api/production`) responden `403` a usuarios con rol Comprador. El aislamiento de datos se aplica por propietario en los casos de uso.

## 3. Tecnologías y arquitectura utilizadas

### Tecnologías

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security (JWT, jjwt), Spring Validation, Spring Boot Actuator |
| Persistencia | PostgreSQL 16, Flyway (migraciones) |
| Documentación API | springdoc-openapi (Swagger UI) |
| Frontend | React 18, TypeScript, Vite 5, Tailwind CSS, React Router, Axios, Lucide React |
| Pruebas | JUnit 5, Mockito, MockMvc, H2 (tests backend), Vitest, Testing Library, jsdom (tests frontend) |
| Calidad frontend | ESLint, TypeScript (`tsc`) |
| Contenedores | Docker, Docker Compose, Nginx |

### Arquitectura

- **Arquitectura hexagonal (puertos y adaptadores):** el dominio y los casos de uso no dependen de frameworks. Las dependencias apuntan hacia el dominio.
- **Backend (paquetes principales):** `domain` (modelo, validaciones, excepciones), `application` (puertos in/out, casos de uso, comandos/modelos), `infrastructure` (adaptadores web, seguridad, persistencia JPA y configuración).
- **Frontend:** SPA en `frontend/src` organizada en `pages`, `components`, `services`, `context`, `lib`, `utils` y `types`.
- **Seguridad:** autenticación sin estado con JWT (HMAC), contraseñas con BCrypt, autorización por rol.
- **Base de datos:** esquema gestionado por Flyway; Hibernate en modo `validate` en ejecución normal. Pruebas backend usan H2 en memoria con `ddl-auto: create-drop` y Flyway deshabilitado.
- **Formato de respuestas API:** envoltorio `{ success, message, data, errors, timestamp }`.

## 4. Requisitos previos y programas necesarios

### Para ejecución local (sin Docker)
- **Java JDK 21**
- **Maven 3.9+**
- **Node.js 20+** y **npm**
- **PostgreSQL 16**
- **Git** (para clonar el repositorio)
- Navegador web moderno

> Nota: el repositorio incluye `backend/target` con artefactos compilados, pero para evaluar el proyecto se recomienda compilar y ejecutar desde el código fuente (`mvn spring-boot:run` / `mvn test`).

### Para ejecución con Docker (alternativa recomendada para despliegue rápido)
- **Docker Engine 24+** y **Docker Compose v2**
- **Git**

## 5. Descarga del proyecto desde GitHub

1. Abra **PowerShell** o **Símbolo del sistema (cmd)**.
2. Elija una carpeta de trabajo y clone el repositorio:

```powershell
git clone <URL_DEL_REPOSITORIO_EN_GITHUB>
cd AGROCONECTA
```

> Reemplace `<URL_DEL_REPOSITORIO_EN_GITHUB>` por la URL real del repositorio proporcionada en GitHub (botón **Code** → HTTPS).

## 6. Configuración de PostgreSQL y la base de datos

### Opción A — PostgreSQL instalado localmente

1. Inicie el servicio de PostgreSQL.
2. Abra una consola SQL (por ejemplo, **psql** o **pgAdmin → Query Tool**) y ejecute:

```sql
CREATE USER agroconecta WITH PASSWORD 'agroconecta';
CREATE DATABASE agroconecta OWNER agroconecta;
```

3. Verifique que PostgreSQL escuche en `localhost:5432` (puerto por defecto).
4. No es necesario crear tablas manualmente: **Flyway** aplica las migraciones (`V1__init_schema.sql`, `V2__seed_demo_data.sql`, `V3__demo_passwords.sql`) automáticamente al arrancar el backend, creando el esquema y cargando los datos de demostración.

> Las credenciales anteriores son las de desarrollo por defecto del proyecto. Si usa otras, deberá ajustarlas en las variables de entorno del backend (ver sección 7).

### Opción B — Solo la base de datos con Docker

Si tiene Docker instalado pero desea ejecutar backend/frontend localmente:

```powershell
docker compose up -d db
```

Esto levanta PostgreSQL 16 en `localhost:5432` con los valores por defecto definidos en `docker-compose.yml` (`agroconecta`/`agroconecta`).

## 7. Configuración y ejecución del backend con Spring Boot

### Configuración

El backend lee su configuración de `backend/src/main/resources/application.yml` con valores por defecto y soporte de variables de entorno:

| Variable | Valor por defecto | Descripción |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/agroconecta` | URL JDBC de PostgreSQL |
| `DB_USER` | `agroconecta` | Usuario de BD |
| `DB_PASSWORD` | `agroconecta` | Contraseña de BD |
| `SERVER_PORT` | `8080` | Puerto HTTP del backend |
| `JWT_SECRET` | (valor de desarrollo en `application.yml`) | Secreto para firmar JWT |
| `JWT_EXPIRATION_MS` | `86400000` | Vigencia del token (ms) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:5174` | Orígenes CORS permitidos |

El archivo `.env.example` en la raíz muestra variables de referencia. **No es necesario crear un `.env` para la ejecución local** si usa los valores por defecto y la base de datos configurada en la sección 6.

Para definir variables en PowerShell (ejemplo, si su BD usa otros datos):
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/agroconecta"
$env:DB_USER="agroconecta"
$env:DB_PASSWORD="su_contraseña"
```

### Ejecución

En una terminal (PowerShell o cmd), desde la raíz del proyecto:

```powershell
cd backend
mvn spring-boot:run
```

Al iniciar correctamente:
- API disponible en: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Salud: `http://localhost:8080/actuator/health`

> El backend aplica las migraciones Flyway al arrancar. Mantenga esta terminal abierta mientras prueba la aplicación.

## 8. Instalación y ejecución del frontend con React, TypeScript, Vite y Tailwind

### Instalación de dependencias

Abra una **nueva** terminal y ejecute:

```powershell
cd frontend
npm install
```

> `npm ci` también es válido si se desea una instalación limpia a partir de `package-lock.json` (requiere que `node_modules` sea consistente; en este repositorio puede usarse cualquiera de los dos).

### Ejecución en desarrollo

```powershell
npm run dev
```

Vite inicia el servidor de desarrollo en `http://localhost:5173` (según `vite.config.ts`).

### Notas de configuración
- El frontend consume la API en la ruta base `/api` (cliente Axios en `src/services/api.ts`).
- En desarrollo, Vite hace **proxy** de `/api` hacia `http://localhost:8080` (configurado en `vite.config.ts`), por lo que no requiere cambiar CORS si se accede desde `http://localhost:5173`.
- No existe `frontend/.env.example`; la variable `VITE_API_URL` puede usarse en escenarios de compilación/despliegue si la API está en otro dominio (ver `docs/06-despliegue.md`).

## 9. Comandos exactos para iniciar la aplicación

Resumen de arranque local (tres pasos, en orden):

**Terminal 1 — Base de datos (si usa Docker solo para la BD):**
```powershell
docker compose up -d db
```
*(Omita este paso si PostgreSQL ya está instalado y en ejecución con la BD creada.)*

**Terminal 2 — Backend:**
```powershell
cd backend
mvn spring-boot:run
```

**Terminal 3 — Frontend:**
```powershell
cd frontend
npm install
npm run dev
```

## 10. Direcciones locales para acceder al sistema

| Componente | Dirección | Notas |
| --- | --- | --- |
| Frontend (desarrollo, Vite) | http://localhost:5173 | Interfaz web principal |
| Backend (API) | http://localhost:8080 | API REST |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentación interactiva |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | Especificación OpenAPI |
| Actuator Health | http://localhost:8080/actuator/health | Estado de la aplicación |
| Frontend (Docker/Nginx) | http://localhost:8081 | Solo en ejecución con Docker Compose |
| PostgreSQL | localhost:5432 | Base de datos (local o contenedor) |

## 11. Usuarios de demostración, credenciales y roles

Datos cargados por las migraciones Flyway (`V2__seed_demo_data.sql` y `V3__demo_passwords.sql`):

| Rol | Nombre | Correo | Contraseña |
| --- | --- | --- | --- |
| Administrador | Administrador AgroConecta | `admin@agroconecta.co` | `Agroconecta123` |
| Productor | María Fernanda Gómez | `maria@agroconecta.co` | `Agroconecta123` |
| Productor | José Luis Martínez | `jose@agroconecta.co` | `Agroconecta123` |
| Comprador | Carlos Andrés Pérez | `carlos@agroconecta.co` | `Agroconecta123` |

> Estas cuentas son de demostración. Se recomienda no utilizarlas en un entorno de producción.

## 12. Recorrido recomendado para probar las funcionalidades

1. **Catálogo público (sin iniciar sesión):**
   - Abra `http://localhost:5173` y acceda a **Explorar catálogo**.
   - Busque por texto y filtre por categoría. Verifique que se muestren productos con precio, cantidad y datos de contacto.

2. **Inicio de sesión como Productor:**
   - Inicie sesión con `maria@agroconecta.co` / `Agroconecta123`.
   - Revise el **Dashboard** (indicadores de parcelas, cultivos, gastos, producción, productos y actividades recientes).

3. **Gestión agrícola (Productor):**
   - **Parcelas:** liste las existentes y cree/edite una parcela (nombre, ubicación, área, descripción).
   - **Cultivos:** asocie un cultivo a una parcela y cambie su estado.
   - **Actividades:** registre una actividad (tipo, fecha, descripción) sobre un cultivo.
   - **Gastos:** registre un gasto con concepto, monto y fecha.
   - **Producción:** registre una cosecha (cantidad y unidad).

4. **Mis productos (Productor):**
   - Publique un producto (nombre, descripción, categoría, precio, cantidad, unidad, contacto).
   - Active/desactive, edite o elimine una publicación y compruebe su reflejo en el catálogo.

5. **Perfil:**
   - Actualice datos del perfil y pruebe el cambio de contraseña (requiere contraseña actual).

6. **Comprador:**
   - Cierre sesión e ingrese con `carlos@agroconecta.co` / `Agroconecta123`.
   - Verifique el dashboard de comprador y el acceso al catálogo.
   - (Opcional, vía API/Swagger) confirme que un comprador no puede acceder a recursos de productor como `/api/parcels` (debe responder `403`).

7. **Administrador:**
   - Inicie sesión con `admin@agroconecta.co` / `Agroconecta123`.
   - En **Usuarios**, liste cuentas, cambie roles o active/desactive un usuario.

8. **Cierre de sesión:** pruebe **Cerrar sesión** desde el menú lateral.

## 13. Ejecución de las pruebas del backend y frontend

### Backend (JUnit 5 + Mockito + MockMvc)

Requisitos: Java 21 y Maven. Las pruebas usan **H2 en memoria** con Flyway deshabilitado (`backend/src/test/resources/application.yml`), por lo que **no requieren PostgreSQL**.

```powershell
cd backend
mvn test
```

Resultado esperado según el proyecto: **51 pruebas** en total (dominio, servicios/casos de uso, controladores, adaptador de persistencia y seguridad de integración).

### Frontend (Vitest + Testing Library)

Requisitos: Node.js y dependencias instaladas (`npm install` en `frontend`).

```powershell
cd frontend
npm run test
```

Comandos adicionales:
```powershell
npm run test:watch
npm run test:coverage
```

Resultado esperado según el proyecto: **38 pruebas** en total.

### Calidad de código frontend (opcional)

```powershell
cd frontend
npm run lint
npm run build
```

`npm run build` ejecuta `tsc -b` y la compilación de Vite.

## 14. Estructura de carpetas del proyecto

```text
AGROCONECTA/
├── .env.example                 # Variables de entorno de ejemplo (Docker/backend)
├── .gitignore
├── docker-compose.yml           # Orquestación: PostgreSQL + backend + frontend
├── README.md                    # Resumen general del proyecto
├── GUIA_PROFESOR.md             # Esta guía
├── backend/                     # API REST Spring Boot (arquitectura hexagonal)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/agroconecta/
│       │   │   ├── domain/          # Modelo, validaciones y excepciones
│       │   │   ├── application/     # Puertos (in/out), casos de uso, comandos/modelos
│       │   │   └── infrastructure/  # Adaptadores web, seguridad, persistencia, config
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/    # Flyway: V1 (esquema), V2 (datos demo), V3 (contraseñas demo)
│       └── test/
│           ├── java/com/agroconecta/...
│           └── resources/application.yml  # Config de pruebas (H2, sin Flyway)
├── frontend/                    # SPA React + TypeScript + Vite + Tailwind
│   ├── Dockerfile
│   ├── nginx.conf               # Servir SPA y proxy /api al backend (Docker)
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── pages/
│       ├── components/
│       ├── services/
│       ├── context/
│       ├── lib/
│       ├── utils/
│       └── types/
└── docs/                        # Documentación adicional
    ├── 01-arquitectura.md
    ├── 02-modelo-de-datos.md
    ├── 03-manual-de-usuario.md
    ├── 04-api.md
    ├── 05-pruebas.md
    └── 06-despliegue.md
```

## 15. Configuración y ejecución con Docker Compose

Requisitos: Docker Engine 24+ y Docker Compose v2.

1. Desde la raíz del proyecto, cree el archivo de variables de entorno:

```powershell
copy .env.example .env
```

2. (Recomendado) Edite `.env` y ajuste al menos `JWT_SECRET` si va a exponer el entorno. Para una prueba local rápida puede usar los valores de ejemplo.
3. Construya y levante los servicios:

```powershell
docker compose up --build
```

Servicios y puertos expuestos (según `docker-compose.yml` y valores por defecto):

| Servicio | Contenedor | Dirección/puerto |
| --- | --- | --- |
| Frontend (Nginx) | `agroconecta-frontend` | http://localhost:8081 |
| Backend (API) | `agroconecta-backend` | http://localhost:8080 |
| PostgreSQL | `agroconecta-db` | localhost:5432 |

Notas:
- Nginx sirve la SPA y redirige `/api/` al backend dentro de la red de Docker (no se requiere configuración adicional de CORS para este escenario).
- Flyway inicializa la base de datos al arrancar el backend.
- Para ejecutar en segundo plano: `docker compose up --build -d`.
- Para detener: `docker compose down` (conserva datos). Para eliminar también el volumen de datos: `docker compose down -v`.

## 16. Solución de errores frecuentes

| Problema | Posible causa | Solución |
| --- | --- | --- |
| `mvn` o `java` no se reconoce | JDK/Maven no instalados o no están en el PATH | Instale Java 21 y Maven 3.9+ y verifique con `java -version` y `mvn -version` |
| `npm` no se reconoce | Node.js no instalado o no está en el PATH | Instale Node.js 20+ y verifique con `node -v` y `npm -v` |
| Backend falla al iniciar (conexión a BD) | PostgreSQL no está activo, BD/usuario no existen o credenciales distintas | Verifique el servicio en el puerto 5432, cree BD/usuario (sección 6) y/o ajuste `DB_URL`, `DB_USER`, `DB_PASSWORD` |
| Puerto 8080, 5173, 8081 o 5432 en uso | Otro proceso ocupa el puerto | Cierre el proceso que usa el puerto o cambie el puerto (`SERVER_PORT`, puerto de Vite, mapeos en `docker-compose.yml`) |
| Error de CORS en el navegador (modo local) | Frontend accedido desde un origen no permitido | Use `http://localhost:5173` en desarrollo (proxy de Vite). Si usa otro origen, ajuste `CORS_ALLOWED_ORIGINS` en el backend |
| Frontend no carga datos en desarrollo | Backend no iniciado o proxy no alcanza `localhost:8080` | Inicie el backend antes y confirme `http://localhost:8080/actuator/health` |
| Login falla con cuentas demo | BD sin datos semilla o migraciones no aplicadas | Asegúrese de usar una BD vacía/nueva gestionada por Flyway al arrancar el backend; revise logs de arranque por errores de migración |
| `401` en peticiones protegidas | Token ausente, inválido o expirado | Inicie sesión nuevamente; la API espera `Authorization: Bearer <token>` |
| `403` al acceder a módulos de productor | Usuario autenticado con rol sin permiso (p. ej. Comprador) | Use una cuenta con rol Productor para esos recursos |
| `docker compose` no funciona | Docker no instalado o Compose v2 no disponible | Use `docker compose` (v2). En instalaciones antiguas pruebe `docker-compose`, si aplica |
| Contenedores no levantan por puertos ocupados | Conflicto con servicios locales | Detenga PostgreSQL/backend/frontend locales o cambie los puertos en `.env`/`docker-compose.yml` y ejecute `docker compose up --build` |

## 17. Consideraciones finales para el profesor

- **Datos y comportamiento reales:** el documento se basa en la configuración y código del repositorio (README, `docs/`, `pom.xml`, `package.json`, `application.yml`, migraciones Flyway, `docker-compose.yml`, Dockerfiles y configuración de Vite/Nginx). Las cuentas, datos de ejemplo y conteos de pruebas indicados corresponden a lo definido en el proyecto.
- **Persistencia:** al ejecutar con Docker Compose, los datos de PostgreSQL se almacenan en el volumen `agroconecta-pgdata` (a menos que se elimine con `docker compose down -v`).
- **Entorno de evaluación:** para una revisión completa se sugiere levantar backend y frontend en local (secciones 6–9), recorrer los flujos por rol (sección 12) y ejecutar las pruebas automatizadas (sección 13).
- **Documentación adicional:** el directorio `docs/` contiene arquitectura, modelo de datos, manual de usuario, guía de API, guía de pruebas y guía de despliegue con mayor detalle técnico.
- **Alcance:** esta guía no modifica el código ni la configuración del proyecto; su propósito es facilitar la descarga, comprensión, ejecución y verificación de AgroConecta.
