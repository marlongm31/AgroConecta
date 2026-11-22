import { api } from '../lib/api';
import type { ValidatedChatAttachment } from '../lib/chatAttachments';
import type {
  ApiResponse,
  ChatConversation,
  ChatMessage,
  PageResponse,
} from '../types';

export interface ChatAttachmentInput extends ValidatedChatAttachment {
  file: File;
}

export interface ChatSendOptions {
  idempotencyKey?: string;
  attachment?: ChatAttachmentInput;
  replyToMessageId?: number | null;
}

function keyHeaders(key?: string): Record<string, string> {
  return key ? { 'Idempotency-Key': key } : {};
}

export const chatService = {
  async start(productId: number): Promise<ChatConversation> {
    const { data } = await api.post<ApiResponse<ChatConversation>>('/chat/conversations', { productId });
    return data.data;
  },
  async list(page = 0, size = 20): Promise<PageResponse<ChatConversation>> {
    const { data } = await api.get<ApiResponse<PageResponse<ChatConversation>>>('/chat/conversations', {
      params: { page, size },
    });
    return data.data;
  },
  async get(id: number): Promise<ChatConversation> {
    const { data } = await api.get<ApiResponse<ChatConversation>>(`/chat/conversations/${id}`);
    return data.data;
  },
  async messages(id: number, after?: number): Promise<ChatMessage[]> {
    const { data } = await api.get<ApiResponse<ChatMessage[]>>(`/chat/conversations/${id}/messages`, {
      params: after ? { after } : {},
    });
    return data.data;
  },
  async send(id: number, content: string | null, options: ChatSendOptions = {}): Promise<ChatMessage> {
    const config = { headers: keyHeaders(options.idempotencyKey) };
    const replyToMessageId = options.replyToMessageId ?? null;
    if (!options.attachment) {
      const { data } = await api.post<ApiResponse<ChatMessage>>(
        `/chat/conversations/${id}/messages`,
        { content, replyToMessageId },
        config,
      );
      return data.data;
    }

    const body = new FormData();
    if (content !== null) body.append('content', content);
    if (replyToMessageId !== null) body.append('replyToMessageId', String(replyToMessageId));
    body.append('file', options.attachment.file, options.attachment.name);
    const { data } = await api.post<ApiResponse<ChatMessage>>(
      `/chat/conversations/${id}/attachments`,
      body,
      {
        ...config,
        headers: {
          ...config.headers,
          'Content-Type': undefined as unknown as string,
        },
      },
    );
    return data.data;
  },
  async downloadAttachment(conversationId: number, messageId: number): Promise<Blob> {
    const { data } = await api.get(
      `/chat/conversations/${conversationId}/attachments/${messageId}`,
      { responseType: 'blob' },
    );
    return data as Blob;
  },
  async unreadCount(): Promise<number> {
    const { data } = await api.get<ApiResponse<{ count: number }>>('/chat/unread-count');
    return data.data.count;
  },
};
