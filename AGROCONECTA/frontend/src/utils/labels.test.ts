import { describe, expect, it } from 'vitest';
import {
  ACTIVITY_TYPE_OPTIONS,
  CROP_STATUS_LABELS,
  CROP_STATUS_OPTIONS,
  MEASUREMENT_UNIT_OPTIONS,
  PRODUCT_CATEGORY_OPTIONS,
  ROLE_LABELS,
  ROLE_OPTIONS,
} from './labels';

describe('label catalogs', () => {
  it('maps every role to a Spanish label', () => {
    expect(ROLE_LABELS.PRODUCTOR).toBe('Productor');
    expect(ROLE_LABELS.COMPRADOR).toBe('Comprador');
    expect(ROLE_LABELS.ADMINISTRADOR).toBe('Administrador');
  });

  it('excludes the administrator role from public registration options', () => {
    expect(ROLE_OPTIONS.map((option) => option.value)).toEqual(['PRODUCTOR', 'COMPRADOR']);
  });

  it('exposes one option per enum value', () => {
    expect(CROP_STATUS_OPTIONS).toHaveLength(5);
    expect(ACTIVITY_TYPE_OPTIONS).toHaveLength(6);
    expect(MEASUREMENT_UNIT_OPTIONS).toHaveLength(5);
    expect(PRODUCT_CATEGORY_OPTIONS).toHaveLength(6);
  });

  it('provides human readable crop status labels', () => {
    expect(CROP_STATUS_LABELS.LISTO_PARA_COSECHA).toBe('Listo para cosecha');
  });
});
