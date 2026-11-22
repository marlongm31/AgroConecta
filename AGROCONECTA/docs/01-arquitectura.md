# Arquitectura de AgroConecta

## 1. Visión general

AgroConecta sigue una **arquitectura hexagonal (puertos y adaptadores)**. El dominio y
los casos de uso no dependen de frameworks: las dependencias siempre apuntan hacia
adentro. Spring Boot, JPA y JWT viven únicamente en la capa de infraestructura.

```
                 +-------------------------------------------------+
                 |                  INFRAESTRUCTURA                 |
   HTTP  ----->  |  Adapter IN (web)  ->  PUERTO IN  (UseCase)      |
                 |                                    |            |
                 |                                    v            |
                 |              APLICACIÓN (servicios de caso de uso)
                 |                                    |            |
                 |                                    v            |
                 |              DOMINIO (modelo + reglas de negocio)
                 |                                    |            |
                 |              PUERTO OUT (RepositoryPort, ...)    |
                 |                                    |            |
   PostgreSQL <--|  Adapter OUT (JPA)  <--------------+            |
                 +-------------------------------------------------+
```

## 2. Capas y paquetes

| Paquete | Responsabilidad | Depende de |
| --- | --- | --- |
| `domain.model` | Entidades de negocio (`User`, `Parcel`, `Crop`, `AgriculturalActivity`, `Expense`, `ProductionRecord`, `Product`, `SoilAssessment`, `ProductImage`, `Conversation`, `Message`) y enums. | Nada |
| `domain.service` | Reglas de orientación agronómica y clima (`SoilAdvisor`, `WeatherAdvisor`). | `domain.model` |
| `domain.validation` | Validadores de reglas de negocio. | `domain.model` |
| `domain.exception` | Excepciones de dominio (`BusinessException`, `NotFoundException`, etc.). | Nada |
| `application.port.in` | Interfaces de casos de uso (`AuthUseCase`, `ParcelUseCase`, ...). | `domain` |
| `application.port.out` | Puertos de salida (`*RepositoryPort`, `PasswordHasherPort`, `TokenProviderPort`). | `domain` |
| `application.usecase` | Implementación de los casos de uso (`*Service`). | `port.in`, `port.out`, `domain` |
| `application.command` / `application.model` | Objetos de entrada y de lectura de los casos de uso. | `domain` |
| `infrastructure.adapter.in.web` | Controladores REST y DTOs. | `port.in` |
| `infrastructure.adapter.in.security` | Filtro JWT, `UserDetailsService`, configuración de seguridad. | `port.in`/`port.out` |
| `infrastructure.adapter.out.persistence` | Entidades JPA, mappers y repositorios que implementan los puertos. | `port.out`, `domain` |
| `infrastructure.config` | Beans de configuración (CORS, OpenAPI, etc.). | — |

## 3. Flujo de una petición

Ejemplo: `POST /api/parcels`

1. El **filtro JWT** valida el token y publica el usuario autenticado en el
   `SecurityContext`.
2. `ParcelController` (adaptador de entrada) valida el DTO y llama a `ParcelUseCase`.
3. `ParcelService` (caso de uso) aplica reglas de dominio y usa `ParcelRepositoryPort`.
4. `ParcelPersistenceAdapter` (adaptador de salida) traduce entre el modelo de dominio y
   la entidad JPA, y persiste en PostgreSQL.
5. La respuesta se envuelve en `ApiResponse<T>`.

## 4. Principios aplicados

- **Inversión de dependencias (DIP):** los casos de uso dependen de interfaces (puertos),
  no de implementaciones concretas.
- **Responsabilidad única (SRP):** cada `*Service` gestiona un único agregado; cada
  controlador, un recurso.
- **Abierto/cerrado (OCP):** añadir un nuevo adaptador (p. ej. gRPC) no requiere tocar el
  dominio.
- **Segregación de interfaces (ISP):** puertos pequeños y específicos por agregado.
- **Inmutabilidad parcial:** los modelos de dominio exponen operaciones de negocio
  (`changeStatus`, `updateProfile`, ...) en lugar de setters públicos indiscriminados.

## 5. Seguridad

- Autenticación sin estado mediante **JWT** firmados con HMAC-SHA512.
- Contraseñas almacenadas con **BCrypt** (adaptador `PasswordHasherPort`).
- Autorización por rol con `@PreAuthorize` / reglas en `SecurityConfig`.
- El aislamiento de datos por productor se garantiza en los casos de uso (cada consulta
  filtra por el `ownerId` del usuario autenticado).

## 6. Decisiones de diseño

- **Flyway** gestiona el esquema; Hibernate se ejecuta en modo `validate` para detectar
  desajustes entre entidades y base de datos.
- **`open-in-view` deshabilitado** para evitar cargas diferidas accidentales fuera de la
  capa de persistencia.
- Los DTOs de la API se mantienen separados de los modelos de dominio para no acoplar el
  contrato externo a los cambios internos.
- El cliente mantiene una cola de operaciones pendientes y una caché por usuario;
  `OfflineContext` expone el estado de conexión y `syncService` reenvía las operaciones
  con claves `Idempotency-Key` para evitar duplicados.
- Las imágenes se guardan en `UPLOAD_DIR` y se sirven desde `/uploads`; Nginx y Vite
  proxyan esa ruta al backend.
- El proveedor climático está detrás de un puerto de salida; Open-Meteo es la
  implementación actual y sus respuestas se cachean en el servidor y en el cliente.
