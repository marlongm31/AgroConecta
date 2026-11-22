import { api } from '../lib/api';
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const authService = {
  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/register', payload);
    return data.data;
  },
  async login(payload: LoginRequest): Promise<AuthResponse> {
    const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/login', payload);
    return data.data;
  },
  async logout(): Promise<void> {
    await api.post('/auth/logout');
  },
};
