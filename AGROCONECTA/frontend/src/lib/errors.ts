import { AxiosError } from 'axios';
import type { ApiResponse } from '../types';

const DEFAULT_MESSAGE = 'Ocurrió un error inesperado. Intenta nuevamente.';

export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    if (!error.response) {
      return 'No fue posible conectar con el servidor. Verifica tu conexión.';
    }
    const body = error.response.data as ApiResponse<unknown> | undefined;
    if (body?.message) {
      return body.message;
    }
    if (error.response.status === 403) {
      return 'No tienes permisos para realizar esta acción.';
    }
    if (error.response.status === 404) {
      return 'El recurso solicitado no existe.';
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return DEFAULT_MESSAGE;
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (error instanceof AxiosError) {
    const body = error.response?.data as ApiResponse<unknown> | undefined;
    if (body?.errors) {
      return body.errors;
    }
  }
  return {};
}
