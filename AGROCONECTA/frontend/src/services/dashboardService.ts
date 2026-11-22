import { api } from '../lib/api';
import type { ApiResponse, BuyerDashboard, ProducerDashboard } from '../types';

export const dashboardService = {
  async producer(): Promise<ProducerDashboard> {
    const { data } = await api.get<ApiResponse<ProducerDashboard>>('/dashboard/producer');
    return data.data;
  },
  async buyer(): Promise<BuyerDashboard> {
    const { data } = await api.get<ApiResponse<BuyerDashboard>>('/dashboard/buyer');
    return data.data;
  },
};
