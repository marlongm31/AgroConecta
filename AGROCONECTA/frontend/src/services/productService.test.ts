import { beforeEach, describe, expect, it, vi, type Mock } from 'vitest';
import { api } from '../lib/api';
import { productService } from './productService';
import type { PageResponse, Product } from '../types';

vi.mock('../lib/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

const apiMock = api as unknown as {
  get: Mock;
  post: Mock;
  put: Mock;
  patch: Mock;
  delete: Mock;
};

const sampleProduct: Product = {
  id: 1,
  ownerId: 2,
  name: 'Tomate chonto',
  description: 'Tomate fresco',
  category: 'HORTALIZAS',
  price: 3500,
  availableQuantity: 120,
  unit: 'KILOGRAMO',
  contactPhone: '+57 300 000 0000',
  contactEmail: 'maria@agroconecta.co',
  active: true,
  soldOut: false,
  imageUrls: [],
};

function envelope<T>(data: T) {
  return { data: { success: true, message: 'ok', data, timestamp: '2026-01-01T00:00:00' } };
}

describe('productService', () => {
  beforeEach(() => {
    apiMock.get.mockReset();
    apiMock.post.mockReset();
  });

  it('requests the public catalog and forwards search and category filters', async () => {
    const page: PageResponse<Product> = {
      content: [sampleProduct],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1,
    };
    apiMock.get.mockResolvedValue(envelope(page));

    const result = await productService.catalog({ search: 'tomate', category: 'HORTALIZAS' });

    expect(apiMock.get).toHaveBeenCalledWith('/products', {
      params: { page: 0, size: 12, search: 'tomate', category: 'HORTALIZAS' },
    });
    expect(result.content).toHaveLength(1);
    expect(result.content[0].name).toBe('Tomate chonto');
  });

  it('omits empty filters from the query', async () => {
    apiMock.get.mockResolvedValue(envelope({ content: [], page: 0, size: 12, totalElements: 0, totalPages: 0 }));
    await productService.catalog();
    expect(apiMock.get).toHaveBeenCalledWith('/products', { params: { page: 0, size: 12 } });
  });

  it('creates a product with the given payload', async () => {
    apiMock.post.mockResolvedValue(envelope(sampleProduct));
    const payload = {
      name: sampleProduct.name,
      description: sampleProduct.description,
      category: sampleProduct.category,
      price: sampleProduct.price,
      availableQuantity: sampleProduct.availableQuantity,
      unit: sampleProduct.unit,
      contactPhone: sampleProduct.contactPhone,
      contactEmail: sampleProduct.contactEmail,
      active: true,
    };

    const created = await productService.create(payload);

    expect(apiMock.post).toHaveBeenCalledWith('/products', payload);
    expect(created.id).toBe(1);
  });

  it('toggles product status through the dedicated endpoint', async () => {
    apiMock.patch.mockResolvedValue(envelope({ ...sampleProduct, active: false }));
    const updated = await productService.changeStatus(1, false);
    expect(apiMock.patch).toHaveBeenCalledWith('/products/1/status', { active: false });
    expect(updated.active).toBe(false);
  });

  it('deletes a product by id', async () => {
    apiMock.delete.mockResolvedValue({});
    await productService.remove(7);
    expect(apiMock.delete).toHaveBeenCalledWith('/products/7');
  });
});
