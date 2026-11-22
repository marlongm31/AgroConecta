import axios from 'axios';
import { storage } from './storage';

const QUEUE_KEY = 'agroconecta.sync.queue';
const CACHE_PREFIX = 'agroconecta.cache';

export type OfflineResource = 'crop' | 'activity' | 'production' | 'chat';
export type OfflineAction = 'create' | 'update' | 'delete';

export interface QueuedOperation {
  id: string;
  resource: OfflineResource;
  action: OfflineAction;
  payload: unknown;
  resourceId?: number;
  createdAt: string;
  attempts: number;
  conflict?: string | null;
  lastError?: string | null;
}

interface CacheEnvelope<T> {
  storedAt: string;
  data: T;
}

function scope(): string {
  const user = storage.getUser<{ id?: number }>();
  return user?.id ? `user-${user.id}` : 'anonymous';
}

export function queueStorageKey(): string {
  return `${QUEUE_KEY}.${scope()}`;
}

export function readQueue(): QueuedOperation[] {
  try {
    const raw = localStorage.getItem(queueStorageKey());
    if (!raw) return [];
    const parsed: unknown = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as QueuedOperation[]) : [];
  } catch {
    return [];
  }
}

export function writeQueue(operations: QueuedOperation[]): void {
  localStorage.setItem(queueStorageKey(), JSON.stringify(operations));
}

export function enqueueOperation(
  resource: OfflineResource,
  action: OfflineAction,
  payload: unknown,
  resourceId?: number,
): QueuedOperation {
  const operation: QueuedOperation = {
    id: createOperationId(),
    resource,
    action,
    payload,
    resourceId,
    createdAt: new Date().toISOString(),
    attempts: 0,
    conflict: null,
    lastError: null,
  };
  writeQueue([...readQueue(), operation]);
  return operation;
}

export function updateQueuedOperation(id: string, changes: Partial<QueuedOperation>): void {
  writeQueue(readQueue().map((operation) => (operation.id === id ? { ...operation, ...changes } : operation)));
}

export function removeQueuedOperation(id: string): void {
  writeQueue(readQueue().filter((operation) => operation.id !== id));
}

export function clearResolvedOperations(): void {
  writeQueue(readQueue().filter((operation) => !operation.conflict));
}

export function createOperationId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID();
  }
  return `op-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

export function isOfflineError(error: unknown): boolean {
  if (!axios.isAxiosError(error)) return false;
  return !error.response || error.response.status >= 503;
}

export function isConflictError(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 409;
}

export function isNotFoundError(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 404;
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message ?? error.message;
  }
  return error instanceof Error ? error.message : 'Error desconocido';
}

export function isOffline(): boolean {
  return typeof navigator !== 'undefined' && !navigator.onLine;
}

export function cacheKey(name: string): string {
  return `${CACHE_PREFIX}.${scope()}.${name}`;
}

export function readCache<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(cacheKey(key));
    if (!raw) return null;
    const envelope = JSON.parse(raw) as CacheEnvelope<T>;
    return envelope.data ?? null;
  } catch {
    return null;
  }
}

export function writeCache<T>(key: string, data: T): void {
  const envelope: CacheEnvelope<T> = { storedAt: new Date().toISOString(), data };
  try {
    localStorage.setItem(cacheKey(key), JSON.stringify(envelope));
  } catch {
    return;
  }
}

export async function withOfflineFallback<T>(key: string, fetcher: () => Promise<T>): Promise<T> {
  try {
    const data = await fetcher();
    writeCache(key, data);
    return data;
  } catch (error) {
    const cached = readCache<T>(key);
    if (cached !== null && (isOffline() || isOfflineError(error))) {
      return cached;
    }
    throw error;
  }
}
