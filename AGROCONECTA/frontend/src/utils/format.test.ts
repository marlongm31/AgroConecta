import { describe, expect, it } from 'vitest';
import { formatCurrency, formatDate, formatDateTime, formatNumber, todayISO } from './format';

describe('format utils', () => {
  it('formats currency in Colombian pesos', () => {
    expect(formatCurrency(3500)).toContain('3.500');
    expect(formatCurrency(0)).toContain('0');
  });

  it('treats null or undefined currency as zero', () => {
    expect(formatCurrency(null)).toContain('0');
    expect(formatCurrency(undefined)).toContain('0');
  });

  it('formats numbers with thousands separators', () => {
    expect(formatNumber(1234.5)).toBe('1.234,5');
  });

  it('formats ISO dates and returns a placeholder for missing values', () => {
    expect(formatDate('2026-03-15')).toContain('2026');
    expect(formatDate(null)).toBe('—');
    expect(formatDate('no-es-fecha')).toBe('—');
  });

  it('formats date-time values', () => {
    expect(formatDateTime('2026-03-15T10:30:00')).toContain('2026');
    expect(formatDateTime(undefined)).toBe('—');
  });

  it('returns an ISO date for today', () => {
    expect(todayISO()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
