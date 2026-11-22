import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { useAuth } from './AuthContext';
import {
  enqueueOperation,
  isOffline,
  readQueue,
  removeQueuedOperation,
  type OfflineAction,
  type OfflineResource,
  type QueuedOperation,
} from '../lib/offline';
import { retryOperation, syncPendingOperations } from '../services/syncService';
import { deleteAttachment } from '../lib/attachmentStore';

interface OfflineContextValue {
  online: boolean;
  syncing: boolean;
  operations: QueuedOperation[];
  pendingCount: number;
  conflictCount: number;
  lastSyncAt: string | null;
  enqueue: (resource: OfflineResource, action: OfflineAction, payload: unknown, resourceId?: number) => QueuedOperation;
  sync: () => Promise<void>;
  retry: (id: string) => Promise<void>;
  discard: (id: string) => void;
  refresh: () => void;
}

const OfflineContext = createContext<OfflineContextValue | undefined>(undefined);

export function OfflineProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [online, setOnline] = useState(() => !isOffline());
  const [syncing, setSyncing] = useState(false);
  const [operations, setOperations] = useState<QueuedOperation[]>(() => readQueue());
  const [lastSyncAt, setLastSyncAt] = useState<string | null>(null);

  const refresh = useCallback(() => setOperations(readQueue()), []);

  const sync = useCallback(
    async () => {
      if (!user || isOffline() || syncing) return;
      setSyncing(true);
      try {
        await syncPendingOperations();
        refresh();
        setLastSyncAt(new Date().toISOString());
      } finally {
        setSyncing(false);
      }
    },
    [refresh, syncing, user],
  );

  useEffect(() => {
    const handleOnline = () => {
      setOnline(true);
      void sync();
    };
    const handleOffline = () => setOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [sync]);

  useEffect(() => {
    refresh();
    if (user && !isOffline()) {
      void sync();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id, refresh]);

  const enqueue = useCallback(
    (resource: OfflineResource, action: OfflineAction, payload: unknown, resourceId?: number) => {
      const operation = enqueueOperation(resource, action, payload, resourceId);
      refresh();
      return operation;
    },
    [refresh],
  );

  const retry = useCallback(
    async (id: string) => {
      await retryOperation(id);
      refresh();
    },
    [refresh],
  );

  const discard = useCallback(
    (id: string) => {
      removeQueuedOperation(id);
      void deleteAttachment(id).catch(() => undefined);
      refresh();
    },
    [refresh],
  );

  const value = useMemo<OfflineContextValue>(
    () => ({
      online,
      syncing,
      operations,
      pendingCount: operations.length,
      conflictCount: operations.filter((operation) => Boolean(operation.conflict)).length,
      lastSyncAt,
      enqueue,
      sync,
      retry,
      discard,
      refresh,
    }),
    [online, syncing, operations, lastSyncAt, enqueue, sync, retry, discard, refresh],
  );

  return <OfflineContext.Provider value={value}>{children}</OfflineContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useOffline(): OfflineContextValue {
  const context = useContext(OfflineContext);
  if (!context) {
    throw new Error('useOffline debe usarse dentro de OfflineProvider');
  }
  return context;
}
