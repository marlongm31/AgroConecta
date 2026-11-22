import { api } from '../lib/api';
import { withOfflineFallback } from '../lib/offline';
import type {
  ApiResponse,
  PageResponse,
  Product,
  ProductCategory,
  ProductImage,
  ProductRequest,
} from '../types';

export interface CatalogQuery {
  search?: string;
  category?: ProductCategory | '';
  page?: number;
  size?: number;
}

export const productService = {
  async catalog(query: CatalogQuery = {}): Promise<PageResponse<Product>> {
    const page = query.page ?? 0;
    const size = query.size ?? 12;
    return withOfflineFallback(`catalog:${page}:${size}:${query.search ?? ''}:${query.category ?? ''}`, async () => {
      const { data } = await api.get<ApiResponse<PageResponse<Product>>>('/products', {
        params: {
          page,
          size,
          ...(query.search ? { search: query.search } : {}),
          ...(query.category ? { category: query.category } : {}),
        },
      });
      return data.data;
    });
  },
  async get(id: number): Promise<Product> {
    const { data } = await api.get<ApiResponse<Product>>(`/products/${id}`);
    return data.data;
  },
  async listMine(page = 0, size = 50): Promise<PageResponse<Product>> {
    return withOfflineFallback(`products-mine:${page}:${size}`, async () => {
      const { data } = await api.get<ApiResponse<PageResponse<Product>>>('/products/mine', {
        params: { page, size },
      });
      return data.data;
    });
  },
  async create(payload: ProductRequest): Promise<Product> {
    const { data } = await api.post<ApiResponse<Product>>('/products', payload);
    return data.data;
  },
  async update(id: number, payload: ProductRequest): Promise<Product> {
    const { data } = await api.put<ApiResponse<Product>>(`/products/${id}`, payload);
    return data.data;
  },
  async changeStatus(id: number, active: boolean): Promise<Product> {
    const { data } = await api.patch<ApiResponse<Product>>(`/products/${id}/status`, { active });
    return data.data;
  },
  async listImages(productId: number): Promise<ProductImage[]> {
    const { data } = await api.get<ApiResponse<ProductImage[]>>(`/products/${productId}/images`);
    return data.data;
  },
  async uploadImage(productId: number, file: File): Promise<ProductImage> {
    const body = new FormData();
    body.append('file', file);
    const { data } = await api.post<ApiResponse<ProductImage>>(`/products/${productId}/images`, body, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data.data;
  },
  async removeImage(imageId: number): Promise<void> {
    await api.delete(`/products/images/${imageId}`);
  },
  async remove(id: number): Promise<void> {
    await api.delete(`/products/${id}`);
  },
};
