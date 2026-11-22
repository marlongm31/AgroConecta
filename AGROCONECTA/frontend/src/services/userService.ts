import { api } from '../lib/api';
import type {
  ApiResponse,
  ChangePasswordRequest,
  PageResponse,
  UpdateProfileRequest,
  User,
} from '../types';

export const userService = {
  async me(): Promise<User> {
    const { data } = await api.get<ApiResponse<User>>('/users/me');
    return data.data;
  },
  async updateProfile(payload: UpdateProfileRequest): Promise<User> {
    const { data } = await api.put<ApiResponse<User>>('/users/me', payload);
    return data.data;
  },
  async changePassword(payload: ChangePasswordRequest): Promise<void> {
    await api.patch('/users/me/password', payload);
  },
  async list(page = 0, size = 10): Promise<PageResponse<User>> {
    const { data } = await api.get<ApiResponse<PageResponse<User>>>('/users', {
      params: { page, size },
    });
    return data.data;
  },
  async updateStatus(id: number, active: boolean): Promise<User> {
    const { data } = await api.patch<ApiResponse<User>>(`/users/${id}/status`, { active });
    return data.data;
  },
  async updateRole(id: number, role: User['role']): Promise<User> {
    const { data } = await api.patch<ApiResponse<User>>(`/users/${id}/role`, { role });
    return data.data;
  },
};
