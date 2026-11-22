# Guía de pruebas

El proyecto incluye **pruebas automatizadas en backend y frontend**. Todas pasan en
verde: **53 pruebas de backend** y **38 pruebas de frontend** (91 en total).

## Backend — JUnit 5 + Mockito + MockMvc

```bash
cd backend
mvn test
```

Configuración: `backend/src/test/resources/application.yml` usa **H2 en memoria**
(`ddl-auto: create-drop`) y **Flyway deshabilitado**, por lo que las pruebas no
necesitan PostgreSQL.

| Clase de prueba | Pruebas | Alcance |
| --- | --- | --- |
| `DomainValidatorTest` | 5 | Reglas de validación de dominio |
| `DomainModelTest` | 7 | Comportamiento de los modelos de dominio |
| `AuthServiceTest` | 6 | Registro, login y gestión de sesión |
| `ParcelServiceTest` | 6 | CRUD de parcelas y aislamiento por productor |
| `ProductServiceTest` | 6 | Publicación, edición y estado de productos |
| `ExpenseServiceTest` | 4 | Registro de gastos |
| `CropServiceTest` | 4 | Gestión de cultivos y estados |
| `SoilAdvisorTest` | 2 | Cultivos desaconsejados y cuidados por cultivo |
| `AuthControllerTest` | 5 | Capa web de autenticación (MockMvc standalone) |
| `PersistenceAdapterTest` | 3 | Adaptador JPA + regresión de búsqueda de productos |
| `SecurityIntegrationTest` | 5 | Seguridad de extremo a extremo (Spring Boot + MockMvc) |

### Prueba de regresión destacada

`PersistenceAdapterTest` cubre la búsqueda de productos por nombre/descripción. En una
versión anterior la consulta provocaba el error PostgreSQL
`function lower(bytea) does not exist`; el caso queda cubierto para evitar regresiones.

## Frontend — Vitest + Testing Library

```bash
cd frontend
npm run test          # ejecución única
npm run test:watch    # modo interactivo
npm run test:coverage # reporte de cobertura
```

Configuración: entorno `jsdom`, archivo de preparación `src/test/setup.ts` y utilidades
`src/test/utils.tsx` (render con `Router`, `AuthProvider`, `ToastProvider` y
`OfflineProvider`).

| Archivo de prueba | Pruebas | Alcance |
| --- | --- | --- |
| `src/utils/format.test.ts` | 6 | Formato de moneda, números y fechas |
| `src/utils/labels.test.ts` | 4 | Catálogos de etiquetas y opciones |
| `src/lib/errors.test.ts` | 8 | Extracción de mensajes y errores de campo |
| `src/lib/storage.test.ts` | 4 | Persistencia de token y usuario |
| `src/services/productService.test.ts` | 5 | Cliente HTTP del catálogo de productos |
| `src/pages/LoginPage.test.tsx` | 3 | Inicio de sesión y errores |
| `src/pages/RegisterPage.test.tsx` | 2 | Registro y rol restringido |
| `src/components/layout/ProtectedRoute.test.tsx` | 3 | Control de acceso por sesión y rol |
| `src/components/catalog/CatalogView.test.tsx` | 3 | Listado, estado vacío y búsqueda |

## Calidad de código

```bash
cd frontend && npm run lint     # ESLint (0 advertencias)
cd frontend && npm run build    # verificación de tipos (tsc) + build
```

## Verificación de integración manual

Con PostgreSQL y el backend en ejecución:

```bash
# Catálogo público
curl http://localhost:8080/api/products

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@agroconecta.co","password":"Agroconecta123"}'
```

Resultados esperados: `200` en catálogo y login, `401` sin token y `403` cuando un
comprador intenta acceder a recursos de productor (p. ej. `/api/parcels`).
