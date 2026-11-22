import type { Product } from '../types';

export type ProductStatus = 'inactive' | 'soldOut' | 'active';

export type ProductStatusTone = 'slate' | 'amber' | 'green';

export const PRODUCT_STATUS_META: Record<ProductStatus, { label: string; tone: ProductStatusTone }> = {
  inactive: { label: 'Inactivo', tone: 'slate' },
  soldOut: { label: 'Agotado', tone: 'amber' },
  active: { label: 'Activo', tone: 'green' },
};

export function getProductStatus(
  product: Pick<Product, 'active' | 'soldOut' | 'availableQuantity'>,
): ProductStatus {
  if (!product.active) return 'inactive';
  if (product.soldOut || product.availableQuantity <= 0) return 'soldOut';
  return 'active';
}
