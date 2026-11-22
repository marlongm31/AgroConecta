import { activityService } from './activityService';
import { chatService } from './chatService';
import { cropService } from './cropService';
import { productionService } from './productionService';
import { deleteAttachment, getAttachment } from '../lib/attachmentStore';
import {
  isConflictError,
  isNotFoundError,
  isOffline,
  readQueue,
  removeQueuedOperation,
  updateQueuedOperation,
  errorMessage,
  type QueuedOperation,
} from '../lib/offline';

export interface SyncResult {
  synced: number;
  conflicts: number;
  failed: number;
  pending: number;
}

interface QueuedChatPayload {
  conversationId: number;
  content: string;
  clientMessageId: string;
  replyToMessageId?: number | null;
  attachment?: {
    name: string;
    contentType: string;
    sizeBytes: number;
  };
}

async function sendOperation(operation: QueuedOperation): Promise<void> {
  switch (operation.resource) {
    case 'crop':
      if (operation.action === 'create') {
        await cropService.create(operation.payload as Parameters<typeof cropService.create>[0], operation.id);
      } else if (operation.action === 'update' && operation.resourceId) {
        await cropService.update(operation.resourceId, operation.payload as Parameters<typeof cropService.update>[1]);
      } else if (operation.action === 'delete' && operation.resourceId) {
        await cropService.remove(operation.resourceId);
      }
      return;
    case 'activity':
      if (operation.action === 'create') {
        await activityService.create(operation.payload as Parameters<typeof activityService.create>[0], operation.id);
      } else if (operation.action === 'update' && operation.resourceId) {
        await activityService.update(operation.resourceId, operation.payload as Parameters<typeof activityService.update>[1]);
      } else if (operation.action === 'delete' && operation.resourceId) {
        await activityService.remove(operation.resourceId);
      }
      return;
    case 'production':
      if (operation.action === 'create') {
        await productionService.create(operation.payload as Parameters<typeof productionService.create>[0], operation.id);
      } else if (operation.action === 'update' && operation.resourceId) {
        await productionService.update(operation.resourceId, operation.payload as Parameters<typeof productionService.update>[1]);
      } else if (operation.action === 'delete' && operation.resourceId) {
        await productionService.remove(operation.resourceId);
      }
      return;
    case 'chat':
      if (operation.action === 'create') {
        const payload = operation.payload as QueuedChatPayload;
        const attachmentKey = payload.clientMessageId || operation.id;
        const file = await getAttachment(attachmentKey);
        if (payload.attachment && !file) {
          throw new Error('El archivo adjunto ya no está disponible en este dispositivo');
        }
        await chatService.send(payload.conversationId, payload.content, {
          idempotencyKey: payload.clientMessageId || operation.id,
          replyToMessageId: payload.replyToMessageId ?? null,
          attachment: file && payload.attachment
            ? {
                file,
                name: payload.attachment.name,
                contentType: payload.attachment.contentType,
                sizeBytes: payload.attachment.sizeBytes,
              }
            : undefined,
        });
        if (file) {
          await deleteAttachment(attachmentKey);
        }
      }
  }
}

export async function syncPendingOperations(): Promise<SyncResult> {
  const operations = readQueue();
  const result: SyncResult = { synced: 0, conflicts: 0, failed: 0, pending: operations.length };
  if (isOffline() || operations.length === 0) {
    return result;
  }

  for (const operation of operations) {
    try {
      await sendOperation(operation);
      removeQueuedOperation(operation.id);
      result.synced += 1;
      result.pending -= 1;
    } catch (error) {
      if (operation.action === 'delete' && isNotFoundError(error)) {
        removeQueuedOperation(operation.id);
        result.synced += 1;
        result.pending -= 1;
        continue;
      }
      if (isConflictError(error)) {
        updateQueuedOperation(operation.id, {
          attempts: operation.attempts + 1,
          conflict: errorMessage(error),
          lastError: errorMessage(error),
        });
        result.conflicts += 1;
      } else {
        updateQueuedOperation(operation.id, {
          attempts: operation.attempts + 1,
          lastError: errorMessage(error),
        });
        result.failed += 1;
      }
    }
  }

  return result;
}

export async function retryOperation(id: string): Promise<SyncResult> {
  updateQueuedOperation(id, { conflict: null, lastError: null });
  return syncPendingOperations();
}
