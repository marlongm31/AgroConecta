# Manual de usuario

## 1. Acceso

1. Abre la aplicación en `http://localhost:5173`.
2. Desde la página de inicio pulsa **Registrarse** o **Iniciar sesión**.
3. Para probar sin crear cuenta usa las credenciales de demostración:

   | Rol | Correo | Contraseña |
   | --- | --- | --- |
   | Administrador | admin@agroconecta.co | `Agroconecta123` |
   | Productor | maria@agroconecta.co | `Agroconecta123` |
   | Comprador | carlos@agroconecta.co | `Agroconecta123` |

## 2. Registro

En **Crear cuenta** completa nombre, correo, contraseña y selecciona el rol
(**Productor** o **Comprador**). La contraseña debe tener al menos 8 caracteres y
combinar letras y números. Teléfono y ubicación son opcionales.

## 3. Panel (Dashboard)

Tras iniciar sesión verás indicadores calculados con datos reales:

- **Productor:** número de parcelas, cultivos activos, total de gastos, registros de
  producción y productos publicados, además de las últimas actividades.
- **Comprador:** número de productos disponibles y publicaciones recientes.

## 4. Módulos del productor

El menú lateral muestra los módulos según el rol del usuario.

- **Parcelas:** registra tus terrenos (nombre, ubicación, área en hectáreas y
  descripción). Cada parcela agrupa cultivos.
- **Cultivos:** asocia un cultivo a una parcela, define fechas de siembra y cosecha
  estimada y gestiona su estado (planificado, en crecimiento, listo para cosecha,
  cosechado o perdido).
- **Actividades:** registra labores agrícolas (siembra, riego, fertilización, poda,
  cosecha u otras) con su fecha y descripción.
- **Gastos:** lleva el control de costos por parcela o cultivo.
- **Producción:** registra las cosechas obtenidas por cultivo (cantidad y unidad de
  medida).
- **Mis productos:** publica tus productos en el catálogo con categoría, precio,
  cantidad disponible, características y datos de contacto. Puedes activarlos,
  desactivarlos, editarlos, añadir hasta seis fotos o eliminarlos. El estado
  **Agotado** se calcula automáticamente cuando la cantidad disponible es cero.
- **Suelo:** responde un cuestionario sobre textura, drenaje, color, retención de agua,
  pendiente, pH, materia orgánica, compactación, salinidad y erosión. La aplicación
  muestra tipo estimado, tratamientos, cultivos adecuados, cuidados por cultivo,
  cultivos desaconsejados y precauciones, además del historial de orientaciones.
- **Clima:** consulta el pronóstico por ubicación o parcela. Muestra temperatura,
  lluvia, viento, alertas para las labores y las siembras previstas en el periodo.

## 5. Catálogo (comprador y visitantes)

- El catálogo es **público**: puede verse sin iniciar sesión desde **Explorar catálogo**.
- Permite buscar por texto (nombre o descripción) y filtrar por categoría.
- Cada tarjeta muestra precio, cantidad disponible, fotos y un botón **Contactar por chat**
  para iniciar una conversación privada. Los datos de contacto directo no se exponen en
  el catálogo público. Solo se muestran productos activos y con existencias.

## 6. Mensajes y trabajo offline

En **Mensajes** puedes ver tus conversaciones, seleccionar una y enviar mensajes de
texto. La lista se actualiza periódicamente y muestra los mensajes no leídos.

Los cultivos, actividades y producción se pueden registrar o editar sin conexión.
Los cambios quedan guardados localmente y se sincronizan automáticamente en segundo
plano cuando vuelve la red, sin exigir una sección ni acciones manuales.

## 7. Perfil

En **Mi perfil** puedes actualizar tu nombre, teléfono y ubicación, y cambiar tu
contraseña indicando la contraseña actual y la nueva.

## 8. Administración de usuarios

El rol **Administrador** accede a **Usuarios**, donde puede listar todas las cuentas,
cambiar roles y activar o desactivar usuarios.

## 9. Cierre de sesión

Pulsa **Cerrar sesión** al pie del menú lateral. Se elimina la sesión local y se
invalida el token.

## 10. Mensajes del sistema

Las operaciones exitosas y los errores se muestran mediante notificaciones emergentes
(toasts). Los formularios indican los campos con errores de validación.
