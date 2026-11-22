import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductCard } from './ProductCard';
import type { Product } from '../../types';

const product: Product = {
  id: 1,
  ownerId: 2,
  name: 'Aguacate Hass',
  description: 'Aguacate fresco',
  category: 'FRUTAS',
  price: 6000,
  availableQuantity: 100,
  unit: 'KILOGRAMO',
  contactPhone: '+57 300 000 0000',
  contactEmail: 'maria@agroconecta.co',
  active: true,
  soldOut: false,
  imageUrls: [],
};

describe('ProductCard', () => {
  it('apila los botones de detalle y chat a ancho completo', () => {
    render(
      <ProductCard
        product={product}
        onViewDetails={vi.fn()}
        onContact={vi.fn()}
      />,
    );

    const details = screen.getByRole('button', { name: 'Ver detalles' });
    const contact = screen.getByRole('button', { name: 'Contactar por chat' });
    const actions = details.parentElement;

    expect(actions?.className).toContain('flex-col');
    expect(actions?.className).not.toContain('sm:flex-row');
    expect(details.className).toContain('w-full');
    expect(details.className).toContain('justify-center');
    expect(contact.className).toContain('w-full');
    expect(contact.className).toContain('justify-center');
  });
});
