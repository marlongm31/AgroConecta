import { describe, expect, it, vi, type Mock } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CatalogView } from './CatalogView';
import { renderWithProviders } from '../../test/utils';
import { productService } from '../../services/productService';
import type { PageResponse, Product } from '../../types';

vi.mock('../../services/productService', () => ({
  productService: {
    catalog: vi.fn(),
    get: vi.fn(),
    listMine: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    changeStatus: vi.fn(),
    remove: vi.fn(),
  },
}));

const serviceMock = productService as unknown as { catalog: Mock };

const product: Product = {
  id: 1,
  ownerId: 2,
  name: 'Aguacate Hass',
  description: 'Aguacate de la región Caribe',
  category: 'FRUTAS',
  price: 6000,
  availableQuantity: 200,
  unit: 'KILOGRAMO',
  contactPhone: '+57 300 000 0000',
  contactEmail: 'maria@agroconecta.co',
  active: true,
  soldOut: false,
  imageUrls: [],
  producerName: 'María Gómez',
  producerLocation: 'Valledupar',
};

function page(products: Product[]): PageResponse<Product> {
  return { content: products, page: 0, size: 12, totalElements: products.length, totalPages: 1 };
}

describe('CatalogView', () => {
  it('lists the products returned by the public catalog', async () => {
    serviceMock.catalog.mockResolvedValue(page([product]));
    renderWithProviders(<CatalogView />);
    expect(await screen.findByText('Aguacate Hass')).toBeInTheDocument();
    expect(screen.getByText('María Gómez')).toBeInTheDocument();
  });

  it('shows an empty state when there are no products', async () => {
    serviceMock.catalog.mockResolvedValue(page([]));
    renderWithProviders(<CatalogView />);
    expect(await screen.findByText('No encontramos productos')).toBeInTheDocument();
  });

  it('re-queries the catalog when the user searches', async () => {
    serviceMock.catalog.mockResolvedValue(page([product]));
    const user1 = userEvent.setup();
    renderWithProviders(<CatalogView />);

    await screen.findByText('Aguacate Hass');
    serviceMock.catalog.mockClear();

    await user1.type(screen.getByLabelText('Buscar producto'), 'café');
    await user1.click(screen.getByRole('button', { name: 'Buscar' }));

    await waitFor(() =>
      expect(serviceMock.catalog).toHaveBeenCalledWith(expect.objectContaining({ search: 'café' })),
    );
  });
});
