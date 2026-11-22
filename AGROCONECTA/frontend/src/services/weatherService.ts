import { api } from '../lib/api';
import { withOfflineFallback } from '../lib/offline';
import type { ApiResponse, WeatherReport } from '../types';

export const weatherService = {
  async byLocation(location: string): Promise<WeatherReport> {
    return withOfflineFallback(`weather:location:${location.toLowerCase()}`, async () => {
      const { data } = await api.get<ApiResponse<WeatherReport>>('/weather', { params: { location } });
      return data.data;
    });
  },
  async byParcel(parcelId: number): Promise<WeatherReport> {
    return withOfflineFallback(`weather:parcel:${parcelId}`, async () => {
      const { data } = await api.get<ApiResponse<WeatherReport>>(`/weather/parcels/${parcelId}`);
      return data.data;
    });
  },
};
