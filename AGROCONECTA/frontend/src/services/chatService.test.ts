import { beforeEach, describe, expect, it, vi, type Mock } from 'vitest';
import { api } from '../lib/api';
import { chatService, type ChatAttachmentInput } from './chatService';
import type { ChatMessage } from '../types';

vi.mock('../lib/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

const apiMock = api as unknown as { get: Mock; post: Mock };

const message: ChatMessage = {
  id: 7,
  conversationId: 2,
  senderId: 10,
  senderName: 'Carlos',
  content: null,
  clientMessageId: 'op-1',
  attachment: {
    id: 9,
    name: 'nota.webm',
    contentType: 'audio/webm',
    sizeBytes: 12,
    downloadUrl: '/api/chat/conversations/2/attachments/7',
  },
  createdAt: '2026-11-17T00:00:00Z',
  mine: true,
  replyToMessageId: 4,
};

function envelope<T>(data: T) {
  return { data: { success: true, message: 'ok', data, timestamp: '2026-11-17T00:00:00Z' } };
}

describe('chatService', () => {
  beforeEach(() => {
    apiMock.get.mockReset();
    apiMock.post.mockReset();
  });

  it('envía una respuesta de texto por JSON con el mensaje citado', async () => {
    apiMock.post.mockResolvedValue(envelope(message));

    await chatService.send(2, 'Sí, confirmed', { replyToMessageId: 4 });

    expect(apiMock.post).toHaveBeenCalledWith(
      '/chat/conversations/2/messages',
      { content: 'Sí, confirmed', replyToMessageId: 4 },
      { headers: {} },
    );
  });

  it('envía audio sin texto y con respuesta como multipart', async () => {
    apiMock.post.mockResolvedValue(envelope(message));
    const file = new File([new Uint8Array([0x1A, 0x45, 0xDF, 0xA3])], 'nota.webm', { type: 'audio/webm' });
    const attachment: ChatAttachmentInput = {
      file,
      name: 'nota.webm',
      contentType: 'audio/webm',
      sizeBytes: file.size,
    };

    const result = await chatService.send(2, null, {
      idempotencyKey: 'op-1',
      attachment,
      replyToMessageId: 4,
    });

    expect(result.id).toBe(7);
    expect(apiMock.post).toHaveBeenCalledTimes(1);
    const [url, body, config] = apiMock.post.mock.calls[0];
    expect(url).toBe('/chat/conversations/2/attachments');
    const form = body as FormData;
    expect(form.has('content')).toBe(false);
    expect(form.get('replyToMessageId')).toBe('4');
    expect(form.get('file')).toBeInstanceOf(File);
    expect(config.headers).toEqual({ 'Idempotency-Key': 'op-1', 'Content-Type': undefined });
  });
});
