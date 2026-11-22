import { AxiosError } from 'axios';
import { describe, expect, it } from 'vitest';
import { getErrorMessage, getFieldErrors } from './errors';

function axiosErrorWithResponse(status: number, data: unknown): AxiosError {
  const error = new AxiosError('Request failed');
  error.response = {
    status,
    data,
    statusText: '',
    headers: {},
    config: {} as never,
  };
  return error;
}

describe('getErrorMessage', () => {
  it('uses the message returned by the API', () => {
    const error = axiosErrorWithResponse(400, { message: 'Datos inválidos', success: false });
    expect(getErrorMessage(error)).toBe('Datos inválidos');
  });

  it('falls back to a permission message on 403', () => {
    const error = axiosErrorWithResponse(403, {});
    expect(getErrorMessage(error)).toBe('No tienes permisos para realizar esta acción.');
  });

  it('falls back to a not found message on 404', () => {
    const error = axiosErrorWithResponse(404, {});
    expect(getErrorMessage(error)).toBe('El recurso solicitado no existe.');
  });

  it('reports connectivity problems when there is no response', () => {
    const error = new AxiosError('Network Error');
    expect(getErrorMessage(error)).toContain('No fue posible conectar con el servidor');
  });

  it('returns the message of plain errors', () => {
    expect(getErrorMessage(new Error('algo falló'))).toBe('algo falló');
  });

  it('returns a default message for unknown values', () => {
    expect(getErrorMessage('boom')).toContain('error inesperado');
  });
});

describe('getFieldErrors', () => {
  it('extracts field level errors', () => {
    const error = axiosErrorWithResponse(400, { errors: { email: 'Correo inválido' } });
    expect(getFieldErrors(error)).toEqual({ email: 'Correo inválido' });
  });

  it('returns an empty object when there are no field errors', () => {
    expect(getFieldErrors(new Error('x'))).toEqual({});
  });
});
