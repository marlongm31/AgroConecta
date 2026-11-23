/**
 * Las imágenes se guardan en el backend como rutas relativas (/uploads/...).
 * Cuando el frontend y el backend están en dominios distintos (Render),
 * hay que anteponer el origen del backend para que el navegador las encuentre.
 */
const apiUrl = import.meta.env.VITE_API_URL ?? '/api';
const backendOrigin = /^https?:\/\//i.test(apiUrl) ? new URL(apiUrl).origin : '';

export function assetUrl(path?: string | null): string {
  if (!path) return '';
  if (/^(https?:|blob:|data:)/i.test(path)) return path;
  return `${backendOrigin}${path}`;
}
