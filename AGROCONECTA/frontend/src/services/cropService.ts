import { api } from '../lib/api';
import { withOfflineFallback } from '../lib/offline';
import type { ApiResponse, Crop, CropRequest, CropStatus, PageResponse } from '../types';

export const cropService = {
  async list(parcelId?: number, page = 0, size = 50): Promise<PageResponse<Crop>> {
    return withOfflineFallback(`crops:${parcelId ?? 'all'}:${page}:${size}`, async () => {
      const { data } = await api.get<ApiResponse<PageResponse<Crop>>>('/crops', {
        params: { page, size, ...(parcelId ? { parcelId } : {}) },
      });
      return data.data;
    });
  },
  async get(id: number): Promise<Crop> {
    const { data } = await api.get<ApiResponse<Crop>>(`/crops/${id}`);
    return data.data;
  },
  async create(payload: CropRequest, idempotencyKey?: string): Promise<Crop> {
    const config = idempotencyKey ? { headers: { 'Idempotency-Key': idempotencyKey } } : undefined;
    const { data } = await api.post<ApiResponse<Crop>>('/crops', payload, config);
    return data.data;
  },
  async update(id: number, payload: CropRequest): Promise<Crop> {
    const { data } = await api.put<ApiResponse<Crop>>(`/crops/${id}`, payload);
    return data.data;
  },
  async changeStatus(id: number, status: CropStatus): Promise<Crop> {
    const { data } = await api.patch<ApiResponse<Crop>>(`/crops/${id}/status`, { status });
    return data.data;
  },
  async remove(id: number): Promise<void> {
    await api.delete(`/crops/${id}`);
  },
};
