import { describe, expect, it } from 'vitest';
import { storage } from './storage';

describe('storage', () => {
  it('stores and retrieves the auth token', () => {
    expect(storage.getToken()).toBeNull();
    storage.setToken('token-123');
    expect(storage.getToken()).toBe('token-123');
  });

  it('serializes and deserializes the user', () => {
    const user = { id: 1, fullName: 'María Gómez', role: 'PRODUCTOR' };
    storage.setUser(user);
    expect(storage.getUser()).toEqual(user);
  });

  it('returns null when the stored user is corrupted', () => {
    localStorage.setItem('agroconecta.user', '{no-json');
    expect(storage.getUser()).toBeNull();
  });

  it('clears both token and user', () => {
    storage.setToken('token-123');
    storage.setUser({ id: 1 });
    storage.clear();
    expect(storage.getToken()).toBeNull();
    expect(storage.getUser()).toBeNull();
  });
});
