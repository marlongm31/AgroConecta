import { describe, expect, it } from 'vitest';
import { getProductStatus } from './productStatus';

describe('getProductStatus', () => {
  it('prioriza Inactivo aunque no haya existencias', () => {
    expect(getProductStatus({ active: false, soldOut: true, availableQuantity: 0 })).toBe('inactive');
  });

  it('marca Agotado cuando está activo pero sin existencias', () => {
    expect(getProductStatus({ active: true, soldOut: true, availableQuantity: 0 })).toBe('soldOut');
    expect(getProductStatus({ active: true, soldOut: false, availableQuantity: 0 })).toBe('soldOut');
  });

  it('marca Activo cuando hay existencias', () => {
    expect(getProductStatus({ active: true, soldOut: false, availableQuantity: 1 })).toBe('active');
  });
});
