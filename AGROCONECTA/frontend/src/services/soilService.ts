import { api } from '../lib/api';
import { withOfflineFallback } from '../lib/offline';
import type {
  ApiResponse,
  PageResponse,
  SoilAssessment,
  SoilAssessmentRequest,
} from '../types';

export const soilService = {
  async assess(payload: SoilAssessmentRequest): Promise<SoilAssessment> {
    const { data } = await api.post<ApiResponse<SoilAssessment>>('/soil/assessments', payload);
    return data.data;
  },
  async list(page = 0, size = 10): Promise<PageResponse<SoilAssessment>> {
    return withOfflineFallback(`soil:${page}:${size}`, async () => {
      const { data } = await api.get<ApiResponse<PageResponse<SoilAssessment>>>('/soil/assessments', {
        params: { page, size },
      });
      return data.data;
    });
  },
  async get(id: number): Promise<SoilAssessment> {
    const { data } = await api.get<ApiResponse<SoilAssessment>>(`/soil/assessments/${id}`);
    return data.data;
  },
};
