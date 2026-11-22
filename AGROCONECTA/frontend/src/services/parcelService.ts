import { api } from '../lib/api';
import type { ApiResponse, PageResponse, Parcel, ParcelRequest } from '../types';

export const parcelService = {
  async list(page = 0, size = 50): Promise<PageResponse<Parcel>> {
    const { data } = await api.get<ApiResponse<PageResponse<Parcel>>>('/parcels', {
      params: { page, size },
    });
    return data.data;
  },
  async get(id: number): Promise<Parcel> {
    const { data } = await api.get<ApiResponse<Parcel>>(`/parcels/${id}`);
    return data.data;
  },
  async create(payload: ParcelRequest): Promise<Parcel> {
    const { data } = await api.post<ApiResponse<Parcel>>('/parcels', payload);
    return data.data;
  },
  async update(id: number, payload: ParcelRequest): Promise<Parcel> {
    const { data } = await api.put<ApiResponse<Parcel>>(`/parcels/${id}`, payload);
    return data.data;
  },
  async remove(id: number): Promise<void> {
    await api.delete(`/parcels/${id}`);
  },
};
