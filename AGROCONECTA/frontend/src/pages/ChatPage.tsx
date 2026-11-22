import {
  ArrowDown,
  ArrowLeft,
  Camera,
  CornerUpLeft,
  FileText,
  Image as ImageIcon,
  MessageCircle,
  Mic,
  Paperclip,
  Pause,
  Play,
  Send,
  Trash2,
  Video,
  WifiOff,
  X,
} from 'lucide-react';
import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
  type KeyboardEvent as ReactKeyboardEvent,
  type MouseEvent as ReactMouseEvent,
  type TouchEvent as ReactTouchEvent,
} from 'react';
import { useSearchParams } from 'react-router-dom';
import { Badge } from '../components/ui/Display';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Button } from '../components/ui/Button';
import { chatService, type ChatAttachmentInput } from '../services/chatService';
import { useAuth } from '../context/AuthContext';
import { useOffline } from '../context/OfflineContext';
import { useUnreadMessages } from '../context/useUnreadMessages';
import { useVoiceRecorder } from '../hooks/useVoiceRecorder';
import { saveAttachment } from '../lib/attachmentStore';
import { formatAttachmentDuration, formatAttachmentSize, validateChatFile } from '../lib/chatAttachments';
import { createOperationId, isOfflineError } from '../lib/offline';
import { getErrorMessage } from '../lib/errors';
import { formatDateTime } from '../utils/format';
import type { ChatAttachment, ChatConversation, ChatMessage } from '../types';

interface PendingAttachmentMeta {
  name: string;
  contentType: string;
  sizeBytes: number;
  durationSeconds?: number;
}

interface QueuedChatPayload {
  conversationId: number;
  content: string;
  clientMessageId: string;
  replyToMessageId?: number | null;
  attachment?: PendingAttachmentMeta;
}

interface ChatBubble {
  id: number | string;
  conversationId?: number;
  mine: boolean;
  content: string | null;
  createdAt: string;
  attachment?: ChatAttachment | PendingAttachmentMeta | null;
  durationSeconds?: number;
  replyToMessageId?: number | null;
  pending?: boolean;
}

interface MediaLightbox {
  kind: 'image' | 'video';
  url: string;
  name: string;
}

function isMediaType(contentType: string): boolean {
  return contentType.startsWith('image/') || contentType.startsWith('video/') || contentType.startsWith('audio/');
}

function attachmentLabel(contentType: string): string {
  if (contentType.startsWith('image/')) return 'Imagen';
  if (contentType.startsWith('video/')) return 'Vídeo';
  if (contentType.startsWith('audio/')) return 'Nota de voz';
  return 'Archivo';
}

function conversationPreview(text: string): string {
  return text
    .replace(/^\[Imagen\].*/, '📷 Foto')
    .replace(/^\[Vídeo\].*/, '🎥 Video')
    .replace(/^\[Audio\].*/, '🎤 Audio')
    .replace(/^\[Archivo\].*/, '📄 Documento');
}

function VideoThumbnail({ url, name, onOpen }: { url: string; name: string; onOpen: () => void }) {
  const [poster, setPoster] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    const capture = () => {
      try {
        if (!video.videoWidth || !video.videoHeight) return;
        const canvas = document.createElement('canvas');
        canvas.width = 320;
        canvas.height = Math.max(1, Math.round((320 * video.videoHeight) / video.videoWidth));
        const context = canvas.getContext('2d');
        if (!context) return;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        setPoster(canvas.toDataURL('image/jpeg', 0.72));
      } catch {
        setPoster(null);
      }
    };
    video.addEventListener('loadeddata', capture);
    video.addEventListener('seeked', capture);
    if (video.readyState >= 2) capture();
    return () => {
      video.removeEventListener('loadeddata', capture);
      video.removeEventListener('seeked', capture);
    };
  }, [url]);

  return (
    <button
      type="button"
      data-chat-video-thumb
      aria-label={`Reproducir ${name}`}
      onClick={onOpen}
      className="relative mb-2 block w-full overflow-hidden rounded-lg bg-slate-900"
    >
      <video
        ref={videoRef}
        src={url}
        preload="metadata"
        muted
        playsInline
        aria-hidden="true"
        className="absolute h-px w-px opacity-0"
      />
      {poster ? (
        <img src={poster} alt="" className="max-h-56 w-full object-cover" />
      ) : (
        <span className="flex h-32 items-center justify-center bg-slate-800 text-slate-300">
          <Video className="h-7 w-7" />
        </span>
      )}
      <span className="absolute inset-0 flex items-center justify-center">
        <span className="flex h-11 w-11 items-center justify-center rounded-full bg-black/60 text-white shadow-lg">
          <Play className="ml-0.5 h-5 w-5 fill-current" />
        </span>
      </span>
    </button>
  );
}

function AudioMessagePlayer({
  url,
  name,
  duration,
  mine,
  onDuration,
}: {
  url: string;
  name: string;
  duration?: number;
  mine: boolean;
  onDuration: (seconds: number) => void;
}) {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [currentTime, setCurrentTime] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [total, setTotal] = useState(duration ?? 0);
  const bars = useMemo(
    () => Array.from({ length: 32 }, (_, index) => {
      const seed = (index + 1) * (name.length + 11) + (index % 7) * 17;
      return 30 + (seed % 65);
    }),
    [name],
  );

  useEffect(() => {
    if (duration && !total) setTotal(duration);
  }, [duration, total]);

  const progress = total > 0 ? Math.min(1, currentTime / total) : 0;

  const togglePlayback = async () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (audio.paused) {
      await audio.play();
    } else {
      audio.pause();
    }
  };

  const seek = (event: ReactMouseEvent<HTMLButtonElement>) => {
    const audio = audioRef.current;
    if (!audio || !total) return;
    const rect = event.currentTarget.getBoundingClientRect();
    const ratio = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width));
    audio.currentTime = ratio * total;
    setCurrentTime(audio.currentTime);
  };

  return (
    <div
      data-chat-audio
      className={`mb-2 flex items-center gap-2 rounded-lg px-2 py-1.5 ${mine ? 'bg-black/10 text-white' : 'bg-slate-100 text-slate-700'}`}
    >
      <audio
        ref={audioRef}
        src={url}
        preload="metadata"
        className="hidden"
        onLoadedMetadata={(event) => {
          const seconds = event.currentTarget.duration;
          if (!Number.isFinite(seconds)) return;
          setTotal(seconds);
          onDuration(seconds);
        }}
        onTimeUpdate={(event) => setCurrentTime(event.currentTarget.currentTime)}
        onPlay={() => setPlaying(true)}
        onPause={() => setPlaying(false)}
        onEnded={() => setPlaying(false)}
      />
      <button
        type="button"
        data-chat-audio-toggle
        aria-label={playing ? 'Pausar nota de voz' : 'Reproducir nota de voz'}
        onClick={() => void togglePlayback()}
        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full transition ${
          mine ? 'bg-white text-agro-700 hover:bg-white/90' : 'bg-agro-600 text-white hover:bg-agro-700'
        }`}
      >
        {playing ? <Pause className="h-4 w-4 fill-current" /> : <Play className="ml-0.5 h-4 w-4 fill-current" />}
      </button>
      <button
        type="button"
        data-chat-audio-wave
        aria-label="Buscar en la nota de voz"
        onClick={seek}
        className="flex h-8 min-w-0 flex-1 items-center gap-[2px]"
      >
        {bars.map((height, index) => {
          const played = index / bars.length <= progress;
          return (
            <span
              key={index}
              data-chat-audio-bar
              className={`w-[3px] shrink-0 rounded-full bg-current transition-all duration-150 ${
                played ? (playing ? 'animate-pulse opacity-100' : 'opacity-100') : 'opacity-25'
              }`}
              style={{ height: `${height}%` }}
            />
          );
        })}
      </button>
      <span data-chat-audio-time className="shrink-0 text-[10px] tabular-nums">
        {formatAttachmentDuration(currentTime)} / {formatAttachmentDuration(total)}
      </span>
      <Mic className="h-3.5 w-3.5 shrink-0 opacity-70" aria-hidden="true" />
    </div>
  );
}

export function ChatPage() {
  const { user } = useAuth();
  const { online, operations, enqueue } = useOffline();
  const { refresh: refreshUnread } = useUnreadMessages();
  const voice = useVoiceRecorder();
  const [searchParams, setSearchParams] = useSearchParams();
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(() => {
    const value = searchParams.get('conversacion');
    return value ? Number(value) : null;
  });
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState('');
  const [attachment, setAttachment] = useState<ChatAttachmentInput | null>(null);
  const [replyTo, setReplyTo] = useState<ChatMessage | null>(null);
  const [attachmentMenuOpen, setAttachmentMenuOpen] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [attachmentUrls, setAttachmentUrls] = useState<Record<number, string>>({});
  const [audioDurations, setAudioDurations] = useState<Record<number, number>>({});
  const [mediaLightbox, setMediaLightbox] = useState<MediaLightbox | null>(null);
  const [swipe, setSwipe] = useState<{ id: number | string; dx: number } | null>(null);
  const [mobileView, setMobileView] = useState<'list' | 'chat'>('list');
  const [showScrollDown, setShowScrollDown] = useState(false);
  const [isDesktopComposer, setIsDesktopComposer] = useState(true);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const imageInputRef = useRef<HTMLInputElement>(null);
  const videoInputRef = useRef<HTMLInputElement>(null);
  const documentInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);
  const attachmentMenuRef = useRef<HTMLDivElement>(null);
  const attachmentUrlsRef = useRef<Record<number, string>>({});
  const messageRefs = useRef<Record<number, HTMLDivElement | null>>({});
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const composerInputRef = useRef<HTMLTextAreaElement>(null);
  const atBottomRef = useRef(true);
  const touchStartRef = useRef<{ id: number | string; x: number; y: number } | null>(null);

  const refreshConversations = async () => {
    const page = await chatService.list(0, 50);
    setConversations(page.content);
    return page.content;
  };

  useEffect(() => {
    let active = true;
    refreshConversations()
      .then((items) => {
        if (!active) return;
        if (!selectedId && items.length) {
          const first = items[0].id;
          setSelectedId(first);
          setSearchParams({ conversacion: String(first) }, { replace: true });
        }
      })
      .catch((caught) => {
        if (active) setError(getErrorMessage(caught));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [selectedId, setSearchParams]);

  useEffect(() => {
    if (!selectedId) return;
    let active = true;
    const load = async () => {
      try {
        const items = await chatService.messages(selectedId);
        if (!active) return;
        setMessages((current) => {
          const merged = new Map(current.map((message) => [message.id, message]));
          items.forEach((message) => merged.set(message.id, message));
          return [...merged.values()].sort((a, b) => a.id - b.id);
        });
        setConversations((current) => current.map((item) => item.id === selectedId ? { ...item, unreadCount: 0 } : item));
        void refreshUnread();
      } catch (caught) {
        if (active) setError(getErrorMessage(caught));
      }
    };
    void load();
    const timer = window.setInterval(() => void load(), 5000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [selectedId, refreshUnread]);

  useEffect(() => {
    let active = true;
    const loadMedia = async () => {
      const needed = new Set(
        messages
          .filter((message) => message.attachment && isMediaType(message.attachment.contentType))
          .map((message) => message.id),
      );
      for (const message of messages) {
        if (!needed.has(message.id) || attachmentUrlsRef.current[message.id]) continue;
        try {
          const blob = await chatService.downloadAttachment(message.conversationId, message.id);
          attachmentUrlsRef.current[message.id] = URL.createObjectURL(blob);
        } catch {
          continue;
        }
      }
      for (const [id, url] of Object.entries(attachmentUrlsRef.current)) {
        if (!needed.has(Number(id))) {
          URL.revokeObjectURL(url);
          delete attachmentUrlsRef.current[Number(id)];
        }
      }
      if (active) setAttachmentUrls({ ...attachmentUrlsRef.current });
    };
    void loadMedia();
    return () => {
      active = false;
    };
  }, [messages]);

  useEffect(() => () => {
    Object.values(attachmentUrlsRef.current).forEach((url) => URL.revokeObjectURL(url));
  }, []);

  useEffect(() => {
    if (!attachment || !isMediaType(attachment.contentType)) {
      setPreviewUrl(null);
      return;
    }
    const url = URL.createObjectURL(attachment.file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [attachment]);

  useEffect(() => {
    const query = window.matchMedia('(min-width: 769px)');
    const update = () => setIsDesktopComposer(query.matches);
    update();
    query.addEventListener('change', update);
    return () => query.removeEventListener('change', update);
  }, []);

  useEffect(() => {
    const input = composerInputRef.current;
    if (!input) return;
    const maxHeight = 20 * 5 + 16;
    input.style.height = 'auto';
    input.style.height = `${Math.min(input.scrollHeight, maxHeight)}px`;
    input.style.overflowY = input.scrollHeight > maxHeight ? 'auto' : 'hidden';
  }, [content, mobileView, selectedId]);

  useEffect(() => {
    if (!attachmentMenuOpen) return;
    const close = (event: MouseEvent) => {
      if (!attachmentMenuRef.current?.contains(event.target as Node)) setAttachmentMenuOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [attachmentMenuOpen]);

  const clearDraft = () => {
    setContent('');
    setAttachment(null);
    setReplyTo(null);
    setError('');
  };

  const selectConversation = (id: number) => {
    setSelectedId(id);
    setMessages([]);
    messageRefs.current = {};
    setAudioDurations({});
    atBottomRef.current = true;
    setShowScrollDown(false);
    setMobileView('chat');
    clearDraft();
    setAttachmentMenuOpen(false);
    setSearchParams({ conversacion: String(id) }, { replace: true });
    setConversations((current) => current.map((item) => item.id === id ? { ...item, unreadCount: 0 } : item));
  };

  const selectFile = async (file?: File | null) => {
    if (!file) return;
    const validation = await validateChatFile(file);
    if (!validation.ok) {
      setError(validation.message);
      return;
    }
    setError('');
    setAttachment({ ...validation.value, file });
    setAttachmentMenuOpen(false);
  };

  const removeAttachment = () => {
    setAttachment(null);
    setError('');
  };

  const queueMessage = async (
    text: string,
    file: ChatAttachmentInput | null,
    replyId: number | null,
    durationSeconds?: number,
  ) => {
    if (!selectedId) return;
    const clientMessageId = createOperationId();
    const payload: QueuedChatPayload = {
      conversationId: selectedId,
      content: text,
      clientMessageId,
      replyToMessageId: replyId,
      ...(file ? {
        attachment: {
          name: file.name,
          contentType: file.contentType,
          sizeBytes: file.sizeBytes,
          ...(durationSeconds ? { durationSeconds } : {}),
        },
      } : {}),
    };
    if (file) {
      try {
        await saveAttachment(clientMessageId, file.file);
      } catch (caught) {
        setError(getErrorMessage(caught));
        return;
      }
    }
    enqueue('chat', 'create', payload, selectedId);
    clearDraft();
  };

  const submitMessage = async (
    text: string,
    file: ChatAttachmentInput | null,
    replyId: number | null,
    durationSeconds?: number,
  ) => {
    if (sending || !selectedId || (!text && !file)) return;
    if (!online) {
      await queueMessage(text, file, replyId, durationSeconds);
      return;
    }

    setSending(true);
    setError('');
    try {
      const message = await chatService.send(selectedId, text || null, {
        idempotencyKey: createOperationId(),
        attachment: file ?? undefined,
        replyToMessageId: replyId,
      });
      setMessages((current) => {
        const withoutReplay = current.filter((item) => item.clientMessageId !== message.clientMessageId);
        return withoutReplay.some((item) => item.id === message.id)
          ? withoutReplay
          : [...withoutReplay, message];
      });
      clearDraft();
      await refreshConversations();
    } catch (caught) {
      if (isOfflineError(caught)) {
        await queueMessage(text, file, replyId, durationSeconds);
      } else {
        setError(getErrorMessage(caught));
      }
    } finally {
      setSending(false);
    }
  };

  const sendFile = async (file: File, durationSeconds?: number) => {
    const validation = await validateChatFile(file);
    if (!validation.ok) {
      setError(validation.message);
      return;
    }
    await submitMessage(
      content.trim(),
      { ...validation.value, file },
      replyTo?.id ?? null,
      durationSeconds,
    );
  };

  const handleSend = async (event: FormEvent) => {
    event.preventDefault();
    await submitMessage(content.trim(), attachment, replyTo?.id ?? null);
  };

  const handleComposerKeyDown = (event: ReactKeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key !== 'Enter') return;
    if (event.shiftKey) {
      event.preventDefault();
      const input = event.currentTarget;
      const start = input.selectionStart ?? content.length;
      const end = input.selectionEnd ?? content.length;
      setContent(`${content.slice(0, start)}\n${content.slice(end)}`);
      window.requestAnimationFrame(() => {
        input.selectionStart = start + 1;
        input.selectionEnd = start + 1;
      });
      return;
    }
    event.preventDefault();
    void submitMessage(content.trim(), attachment, replyTo?.id ?? null);
  };

  const startVoice = async () => {
    setError('');
    setAttachmentMenuOpen(false);
    try {
      const result = await voice.start();
      if (!result) return;
      await sendFile(result.file, result.durationSeconds);
    } catch (caught) {
      setError(getErrorMessage(caught));
    }
  };

  const downloadFile = async (conversationId: number, messageId: number, name: string) => {
    try {
      const blob = await chatService.downloadAttachment(conversationId, messageId);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = name;
      link.click();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (caught) {
      setError(getErrorMessage(caught));
    }
  };

  const selected = conversations.find((item) => item.id === selectedId);
  const pendingOperations = operations.filter(
    (operation) => operation.resource === 'chat'
      && operation.action === 'create'
      && operation.resourceId === selectedId,
  );
  const pendingKeys = new Set(
    pendingOperations.map((operation) => (operation.payload as QueuedChatPayload).clientMessageId),
  );
  const bubbles: ChatBubble[] = [
    ...messages
      .filter((message) => !message.clientMessageId || !pendingKeys.has(message.clientMessageId))
      .map((message) => ({
        id: message.id,
        conversationId: message.conversationId,
        mine: message.mine,
        content: message.content,
        createdAt: message.createdAt,
        attachment: message.attachment,
        replyToMessageId: message.replyToMessageId,
      })),
    ...pendingOperations.map((operation) => {
      const payload = operation.payload as QueuedChatPayload;
      return {
        id: `pending-${operation.id}`,
        mine: true,
        content: payload.content,
        createdAt: operation.createdAt,
        attachment: payload.attachment ?? null,
        durationSeconds: payload.attachment?.durationSeconds,
        replyToMessageId: payload.replyToMessageId,
        pending: true,
      };
    }),
  ].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());

  const messageById = new Map(messages.map((message) => [message.id, message]));

  const scrollMessagesToBottom = (behavior: ScrollBehavior = 'auto') => {
    const container = messagesContainerRef.current;
    if (!container) return;
    container.scrollTo({ top: container.scrollHeight, behavior });
    atBottomRef.current = true;
    setShowScrollDown(false);
  };

  const handleMessagesScroll = () => {
    const container = messagesContainerRef.current;
    if (!container) return;
    const atBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 120;
    atBottomRef.current = atBottom;
    setShowScrollDown(!atBottom && container.scrollHeight > container.clientHeight + 40);
  };

  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container || !atBottomRef.current) return;
    const frame = window.requestAnimationFrame(() => {
      container.scrollTo({ top: container.scrollHeight, behavior: 'auto' });
      setShowScrollDown(false);
    });
    return () => window.cancelAnimationFrame(frame);
  }, [selectedId, bubbles.length]);

  const scrollToMessage = (id: number) => {
    const target = messageRefs.current[id];
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'center' });
      return;
    }
    setError('El mensaje original ya no está disponible en la conversación cargada.');
  };

  const replyPreview = (message: ChatMessage | null) => {
    if (!message) return '';
    if (message.content?.trim()) return message.content.trim();
    if (message.attachment) return `[${attachmentLabel(message.attachment.contentType)}] ${message.attachment.name}`;
    return 'Mensaje';
  };

  const renderReplyQuote = (bubble: ChatBubble) => {
    if (!bubble.replyToMessageId) return null;
    const original = messageById.get(bubble.replyToMessageId);
    return (
      <button
        type="button"
        data-chat-reply-quote
        onClick={() => scrollToMessage(bubble.replyToMessageId as number)}
        className={`mb-2 block w-full rounded border-l-2 px-2 py-1 text-left text-[11px] ${
          bubble.mine ? 'border-agro-200 bg-agro-700/40 text-agro-50' : 'border-agro-400 bg-slate-100 text-slate-600'
        }`}
      >
        <span className="block font-semibold">{original?.senderName ?? 'Mensaje original'}</span>
        <span className="block truncate">{original ? replyPreview(original) : 'Mensaje no disponible'}</span>
      </button>
    );
  };

  const durationForBubble = (bubble: ChatBubble): number | undefined => {
    if (typeof bubble.id !== 'number') return bubble.durationSeconds;
    return audioDurations[bubble.id] ?? bubble.durationSeconds;
  };

  const renderAttachment = (bubble: ChatBubble) => {
    const item = bubble.attachment;
    if (!item) return null;
    const contentType = item.contentType;
    const url = bubble.conversationId ? attachmentUrls[Number(bubble.id)] : null;

    if (contentType.startsWith('image/')) {
      if (url) {
        return (
          <button
            type="button"
            onClick={() => setMediaLightbox({ kind: 'image', url, name: item.name })}
            className="mb-2 block max-w-full overflow-hidden rounded-lg"
          >
            <img src={url} alt={item.name} className="max-h-48 max-w-full object-contain" />
          </button>
        );
      }
      return (
        <div className="mb-2 flex items-center gap-2 rounded-lg bg-black/10 px-2 py-1.5 text-xs">
          <ImageIcon className="h-4 w-4 shrink-0" />
          <span className="truncate">{item.name}</span>
        </div>
      );
    }

    if (contentType.startsWith('video/')) {
      if (url) {
        return (
          <VideoThumbnail
            url={url}
            name={item.name}
            onOpen={() => setMediaLightbox({ kind: 'video', url, name: item.name })}
          />
        );
      }
      return (
        <div className="mb-2 flex items-center gap-2 rounded-lg bg-black/10 px-2 py-1.5 text-xs">
          <Video className="h-4 w-4 shrink-0" />
          <span className="truncate">{item.name}</span>
        </div>
      );
    }

    if (contentType.startsWith('audio/')) {
      const duration = durationForBubble(bubble);
      if (url) {
        return (
          <AudioMessagePlayer
            url={url}
            name={item.name}
            duration={duration}
            mine={bubble.mine}
            onDuration={(seconds) => {
              if (typeof bubble.id !== 'number') return;
              setAudioDurations((current) => current[bubble.id as number] === seconds
                ? current
                : { ...current, [bubble.id as number]: seconds });
            }}
          />
        );
      }
      return (
        <div className="mb-2 flex items-center gap-2 rounded-lg bg-black/10 px-2 py-1.5 text-xs">
          <Mic className="h-4 w-4 shrink-0" />
          <span className="truncate">{item.name}</span>
          {duration ? <span className="shrink-0">{formatAttachmentDuration(duration)}</span> : null}
        </div>
      );
    }

    if (bubble.conversationId && 'downloadUrl' in item) {
      return (
        <button
          type="button"
          onClick={() => void downloadFile(bubble.conversationId as number, Number(bubble.id), item.name)}
          className="mb-2 flex w-full items-center gap-2 rounded-lg bg-black/10 px-2 py-1.5 text-left text-xs hover:bg-black/20"
        >
          <FileText className="h-4 w-4 shrink-0" />
          <span className="truncate">{item.name}</span>
        </button>
      );
    }
    return (
      <div className="mb-2 flex items-center gap-2 rounded-lg bg-black/10 px-2 py-1.5 text-xs">
        <FileText className="h-4 w-4 shrink-0" />
        <span className="truncate">{item.name}</span>
      </div>
    );
  };

  const handleTouchStart = (event: ReactTouchEvent, id: number | string) => {
    const touch = event.touches[0];
    touchStartRef.current = { id, x: touch.clientX, y: touch.clientY };
  };

  const handleTouchMove = (event: ReactTouchEvent, id: number | string) => {
    const start = touchStartRef.current;
    if (!start || start.id !== id) return;
    const touch = event.touches[0];
    const deltaX = touch.clientX - start.x;
    const deltaY = touch.clientY - start.y;
    setSwipe(deltaX > 0 && deltaX > Math.abs(deltaY) ? { id, dx: Math.min(deltaX, 80) } : null);
  };

  const handleTouchEnd = (event: ReactTouchEvent, bubble: ChatBubble) => {
    const start = touchStartRef.current;
    touchStartRef.current = null;
    setSwipe(null);
    if (!start || start.id !== bubble.id) return;
    const touch = event.changedTouches[0];
    const deltaX = touch.clientX - start.x;
    const deltaY = touch.clientY - start.y;
    if (deltaX < 60 || Math.abs(deltaY) > 40 || typeof bubble.id !== 'number') return;
    const original = messageById.get(bubble.id);
    if (original) setReplyTo(original);
  };

  const hasDraft = Boolean(content.trim() || attachment);

  return (
    <div className="chat-page flex min-h-0 flex-1 flex-col overflow-hidden">
      <div className="flex shrink-0 items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 py-1.5">
        <div className="min-w-0">
          <h1 className="truncate text-base font-bold text-slate-900">Mensajes</h1>
          <p className="hidden truncate text-[11px] text-slate-500 sm:block">
            Comunícate con productores y compradores sobre los productos del catálogo.
          </p>
        </div>
      </div>

      {!online ? (
        <div className="flex shrink-0 items-center gap-2 bg-amber-50 px-4 py-1 text-xs text-amber-800">
          <WifiOff className="h-3.5 w-3.5" /> Sin conexión. Mensajes y archivos se enviarán al volver la red.
        </div>
      ) : null}

      {loading ? <PageLoader /> : (
        <div className="chat-grid flex min-h-0 flex-1 overflow-hidden p-0 lg:gap-4 lg:p-3">
          <section className={`card flex min-h-0 flex-col overflow-hidden ${mobileView === 'chat' ? 'hidden lg:flex' : 'flex'}`}>
            <div className="shrink-0 border-b border-slate-100 px-4 py-2">
              <div className="flex items-center justify-between gap-2">
                <h2 className="text-sm font-semibold text-slate-800">Conversaciones</h2>
                <span className="text-[11px] text-slate-400">{conversations.length}</span>
              </div>
            </div>
            {conversations.length === 0 ? (
              <div className="p-4">
                <EmptyState
                  icon={<MessageCircle className="h-8 w-8" />}
                  title="Sin conversaciones"
                  description="Abre un producto del catálogo y pulsa «Contactar por chat» para empezar."
                />
              </div>
            ) : (
              <ul data-chat-conversations className="min-h-0 flex-1 overflow-y-auto overscroll-contain">
                {conversations.map((conversation) => (
                  <li key={conversation.id}>
                    <button
                      type="button"
                      onClick={() => selectConversation(conversation.id)}
                      className={`flex w-full items-start gap-3 border-b border-slate-100 px-4 py-3 text-left transition ${
                        conversation.id === selectedId ? 'bg-agro-50' : 'hover:bg-slate-50'
                      }`}
                    >
                      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-agro-100 text-sm font-bold text-agro-700">
                        {conversation.counterpartName.charAt(0).toUpperCase()}
                      </span>
                      <span className="min-w-0 flex-1">
                        <span className="flex items-center justify-between gap-2">
                          <span className="truncate text-sm font-semibold text-slate-800">{conversation.counterpartName}</span>
                          {conversation.unreadCount > 0 ? (
                            <span data-chat-unread-badge><Badge tone="green">{conversation.unreadCount}</Badge></span>
                          ) : null}
                        </span>
                        <span className="mt-0.5 block truncate text-xs text-slate-500">{conversation.productName ?? 'Producto'}</span>
                        <span className="mt-1 block truncate text-xs text-slate-400">
                          {conversationPreview(conversation.lastMessage || 'Sin mensajes')}
                        </span>
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className={`card flex min-h-0 flex-col overflow-hidden ${mobileView === 'chat' ? 'flex' : 'hidden lg:flex'}`}>
            {selected ? (
              <>
                <div className="flex shrink-0 items-center justify-between gap-2 border-b border-slate-100 px-3 py-2">
                  <div className="flex min-w-0 items-center gap-2">
                    <button
                      type="button"
                      data-chat-back
                      aria-label="Volver a conversaciones"
                      onClick={() => setMobileView('list')}
                      className="rounded-full p-1.5 text-slate-600 hover:bg-slate-100 lg:hidden"
                    >
                      <ArrowLeft className="h-5 w-5" />
                    </button>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-slate-900">{selected.counterpartName}</p>
                      <p className="truncate text-xs text-slate-500">{selected.productName ?? 'Producto del catálogo'}</p>
                    </div>
                  </div>
                  <div className="hidden shrink-0 sm:block">
                    <Badge tone="slate">{user?.fullName}</Badge>
                  </div>
                </div>

                <div className="relative min-h-0 flex-1">
                  <div
                    ref={messagesContainerRef}
                    data-chat-messages
                    onScroll={handleMessagesScroll}
                    className="h-full space-y-3 overflow-y-auto overscroll-contain bg-slate-50 px-4 pb-6 pt-4"
                  >
                    {bubbles.length === 0 ? (
                      <div data-chat-empty className="flex h-full w-full items-center justify-center px-4 py-8 text-center">
                        <p className="max-w-md text-sm text-slate-400">Escribe el primer mensaje para iniciar la conversación.</p>
                      </div>
                    ) : bubbles.map((bubble) => (
                      <div
                        key={bubble.id}
                        ref={typeof bubble.id === 'number' ? (node) => {
                          messageRefs.current[bubble.id as number] = node;
                        } : undefined}
                        className={`group relative flex ${bubble.mine ? 'justify-end' : 'justify-start'}`}
                        style={{ touchAction: 'pan-y' }}
                        onTouchStart={(event) => handleTouchStart(event, bubble.id)}
                        onTouchMove={(event) => handleTouchMove(event, bubble.id)}
                        onTouchEnd={(event) => handleTouchEnd(event, bubble)}
                      >
                        {swipe?.id === bubble.id && swipe.dx > 12 ? (
                          <span className="absolute left-0 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-agro-100 text-agro-700">
                            <CornerUpLeft className="h-4 w-4" />
                          </span>
                        ) : null}
                        <div
                          className={`relative max-w-[80%] rounded-2xl px-4 py-2 text-sm ${
                            typeof bubble.id === 'number' ? 'pr-7' : ''
                          } ${
                            bubble.mine ? 'rounded-br-sm bg-agro-600 text-white' : 'rounded-bl-sm bg-white text-slate-700 shadow-sm'
                          }`}
                          style={{
                            transform: swipe?.id === bubble.id ? `translateX(${swipe.dx}px)` : undefined,
                          }}
                        >
                          {typeof bubble.id === 'number' ? (
                            <button
                              type="button"
                              data-chat-reply
                              aria-label="Responder mensaje"
                              onClick={() => {
                                const original = messageById.get(bubble.id as number);
                                if (original) setReplyTo(original);
                              }}
                              className="absolute right-1 top-1 z-10 hidden h-5 w-5 items-center justify-center rounded-full bg-white/90 text-slate-600 shadow-sm hover:bg-agro-50 group-hover:flex"
                            >
                              <CornerUpLeft className="h-3 w-3" />
                            </button>
                          ) : null}
                          {renderReplyQuote(bubble)}
                          {renderAttachment(bubble)}
                          {bubble.content ? <p className="whitespace-pre-wrap">{bubble.content}</p> : null}
                          <p className={`mt-1 text-[10px] ${bubble.mine ? 'text-agro-100' : 'text-slate-400'}`}>
                            {formatDateTime(bubble.createdAt)}
                            {bubble.pending ? ' · Pendiente de sincronización' : ''}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                  {showScrollDown ? (
                    <button
                      type="button"
                      data-chat-scroll-down
                      aria-label="Ir al último mensaje"
                      onClick={() => scrollMessagesToBottom()}
                      className="absolute bottom-4 right-4 flex h-10 w-10 items-center justify-center rounded-full bg-white text-agro-700 shadow-lg ring-1 ring-slate-200 transition hover:bg-agro-50"
                    >
                      <ArrowDown className="h-5 w-5" />
                    </button>
                  ) : null}
                </div>

                {error ? <p className="shrink-0 px-4 pb-1 text-xs text-red-700">{error}</p> : null}
                <div className="chat-composer shrink-0 border-t border-slate-100 bg-white px-3 py-2">
                  {replyTo ? (
                    <div className="mb-2 flex items-center gap-2 rounded-lg border-l-4 border-agro-500 bg-agro-50 px-3 py-2 text-xs">
                      <CornerUpLeft className="h-4 w-4 shrink-0 text-agro-700" />
                      <div className="min-w-0 flex-1">
                        <p className="font-semibold text-agro-800">Respondiendo a {replyTo.senderName}</p>
                        <p className="truncate text-slate-600">{replyPreview(replyTo)}</p>
                      </div>
                      <button
                        type="button"
                        aria-label="Cancelar respuesta"
                        onClick={() => setReplyTo(null)}
                        className="rounded-full p-1.5 text-slate-500 hover:bg-slate-100"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    </div>
                  ) : null}

                  {attachment ? (
                    <div className="mb-2 flex items-center gap-3 rounded-lg border border-slate-200 p-2">
                      {previewUrl && attachment.contentType.startsWith('image/') ? (
                        <img src={previewUrl} alt="Vista previa" className="h-12 w-12 rounded object-contain" />
                      ) : previewUrl && attachment.contentType.startsWith('video/') ? (
                        <video src={previewUrl} muted className="h-12 w-12 rounded bg-slate-100 object-cover" />
                      ) : previewUrl && attachment.contentType.startsWith('audio/') ? (
                        <div className="flex h-8 items-center gap-2 rounded bg-slate-100 px-2 text-xs text-slate-600">
                          <Mic className="h-4 w-4 shrink-0" />
                          <span>Nota de voz</span>
                        </div>
                      ) : (
                        <FileText className="h-6 w-6 text-slate-500" />
                      )}
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-xs font-medium text-slate-700">{attachment.name}</p>
                        <p className="text-[11px] text-slate-400">{formatAttachmentSize(attachment.sizeBytes)}</p>
                      </div>
                      <button
                        type="button"
                        aria-label="Quitar archivo adjunto"
                        onClick={removeAttachment}
                        className="rounded-full p-1.5 text-slate-500 hover:bg-slate-100"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    </div>
                  ) : null}

                  <input
                    ref={imageInputRef}
                    data-chat-file="image"
                    type="file"
                    accept="image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp"
                    className="hidden"
                    onChange={(event) => {
                      const file = event.target.files?.[0] ?? null;
                      event.target.value = '';
                      void selectFile(file);
                    }}
                  />
                  <input
                    ref={videoInputRef}
                    data-chat-file="video"
                    type="file"
                    accept="video/mp4,video/webm,.mp4,.webm"
                    className="hidden"
                    onChange={(event) => {
                      const file = event.target.files?.[0] ?? null;
                      event.target.value = '';
                      void selectFile(file);
                    }}
                  />
                  <input
                    ref={documentInputRef}
                    data-chat-file="document"
                    type="file"
                    accept=".pdf,.doc,.docx,.xls,.xlsx"
                    className="hidden"
                    onChange={(event) => {
                      const file = event.target.files?.[0] ?? null;
                      event.target.value = '';
                      void selectFile(file);
                    }}
                  />
                  <input
                    ref={cameraInputRef}
                    data-chat-file="camera"
                    type="file"
                    accept="image/jpeg,image/png,image/webp"
                    capture="environment"
                    className="hidden"
                    onChange={(event) => {
                      const file = event.target.files?.[0] ?? null;
                      event.target.value = '';
                      void selectFile(file);
                    }}
                  />

                  {voice.state !== 'idle' ? (
                    <div className="flex items-center gap-3 rounded-full bg-red-50 px-3 py-2 text-sm text-red-800">
                      <button
                        type="button"
                        data-chat-voice-cancel
                        aria-label="Cancelar grabación"
                        onClick={voice.cancel}
                        className="flex h-8 w-8 items-center justify-center rounded-full text-red-600 hover:bg-red-100"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                      <span className="flex items-center gap-2">
                        <span className="h-2 w-2 animate-pulse rounded-full bg-red-500" />
                        <span data-chat-voice-timer>
                          {voice.state === 'requesting' ? 'Activando micrófono...' : formatAttachmentDuration(voice.elapsed)}
                        </span>
                      </span>
                      <button
                        type="button"
                        data-chat-voice-stop
                        aria-label="Enviar nota de voz"
                        onClick={voice.stop}
                        disabled={voice.state !== 'recording'}
                        className="ml-auto flex h-9 w-9 items-center justify-center rounded-full bg-agro-600 text-white transition hover:bg-agro-700 disabled:opacity-50"
                      >
                        <Send className="h-4 w-4" />
                      </button>
                    </div>
                  ) : (
                    <form onSubmit={(event) => void handleSend(event)}>
                      <div className="flex items-end gap-2 md:gap-0 md:rounded-full md:bg-slate-100 md:px-2 md:py-1">
                        <div className="order-1 flex min-w-0 flex-1 items-end gap-1 rounded-full bg-slate-100 px-2 py-1 md:order-2 md:bg-transparent md:px-0 md:py-0">
                          <textarea
                            ref={composerInputRef}
                            data-chat-input
                            name="content"
                            rows={1}
                            value={content}
                            onChange={(event) => setContent(event.target.value)}
                            onKeyDown={handleComposerKeyDown}
                            placeholder={isDesktopComposer ? 'Escribe un mensaje' : 'Mensaje'}
                            className="chat-composer-input order-1 min-w-0 flex-1 bg-transparent px-2 py-1.5 text-sm leading-5 text-slate-800 outline-none placeholder:text-slate-400 md:order-2"
                          />
                          <div className="relative order-2 shrink-0 md:order-1" ref={attachmentMenuRef}>
                            <Button
                              type="button"
                              variant="ghost"
                              aria-label="Adjuntar archivo"
                              data-chat-attach
                              className="rounded-full p-1.5 text-slate-500 hover:bg-slate-200"
                              icon={<Paperclip className="h-4 w-4" />}
                              onClick={() => setAttachmentMenuOpen((open) => !open)}
                            />
                            {attachmentMenuOpen ? (
                              <div className="absolute bottom-full right-0 z-20 mb-2 w-44 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-lg">
                                <button
                                  type="button"
                                  onClick={() => imageInputRef.current?.click()}
                                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50"
                                >
                                  <ImageIcon className="h-4 w-4" /> Imagen
                                </button>
                                <button
                                  type="button"
                                  onClick={() => videoInputRef.current?.click()}
                                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50"
                                >
                                  <Video className="h-4 w-4" /> Video
                                </button>
                                <button
                                  type="button"
                                  onClick={() => documentInputRef.current?.click()}
                                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50"
                                >
                                  <FileText className="h-4 w-4" /> Documento
                                </button>
                                <button
                                  type="button"
                                  onClick={() => cameraInputRef.current?.click()}
                                  className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-50"
                                >
                                  <Camera className="h-4 w-4" /> Cámara
                                </button>
                              </div>
                            ) : null}
                          </div>
                        </div>
                        {hasDraft ? (
                          <>
                            <button
                              type="submit"
                              data-chat-send
                              aria-label="Enviar mensaje"
                              disabled={sending}
                              className="order-2 flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-agro-600 text-white transition hover:bg-agro-700 disabled:opacity-60 md:hidden"
                            >
                              <Send className="h-5 w-5" />
                            </button>
                            <button
                              type="submit"
                              data-chat-send
                              aria-label="Enviar mensaje"
                              disabled={sending}
                              className="hidden h-8 w-8 shrink-0 items-center justify-center rounded-full text-agro-600 transition hover:bg-agro-100 disabled:opacity-60 md:order-3 md:flex"
                            >
                              <Send className="h-5 w-5" />
                            </button>
                          </>
                        ) : (
                          <>
                            <button
                              type="button"
                              data-chat-voice
                              aria-label="Grabar nota de voz"
                              onClick={() => void startVoice()}
                              className="order-2 flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-agro-600 text-white transition hover:bg-agro-700 md:hidden"
                            >
                              <Mic className="h-5 w-5" />
                            </button>
                            <button
                              type="button"
                              data-chat-voice
                              aria-label="Grabar nota de voz"
                              onClick={() => void startVoice()}
                              className="hidden h-8 w-8 shrink-0 items-center justify-center rounded-full text-agro-600 transition hover:bg-agro-100 md:order-3 md:flex"
                            >
                              <Mic className="h-5 w-5" />
                            </button>
                          </>
                        )}
                      </div>
                    </form>
                  )}
                </div>
              </>
            ) : (
              <div className="flex flex-1 items-center justify-center p-6">
                <EmptyState
                  icon={<MessageCircle className="h-8 w-8" />}
                  title="Selecciona una conversación"
                  description="Aquí verás los mensajes intercambiados."
                />
              </div>
            )}
          </section>
        </div>
      )}

      {mediaLightbox ? (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4"
          onClick={() => setMediaLightbox(null)}
        >
          <div className="relative max-h-full max-w-full" onClick={(event) => event.stopPropagation()}>
            {mediaLightbox.kind === 'video' ? (
              <video
                src={mediaLightbox.url}
                controls
                autoPlay
                aria-label={mediaLightbox.name}
                className="max-h-[85vh] max-w-[90vw]"
              />
            ) : (
              <img src={mediaLightbox.url} alt={mediaLightbox.name} className="max-h-[85vh] max-w-[90vw] object-contain" />
            )}
            <button
              type="button"
              aria-label={mediaLightbox.kind === 'video' ? 'Cerrar vídeo' : 'Cerrar imagen'}
              onClick={() => setMediaLightbox(null)}
              className="absolute -right-3 -top-3 rounded-full bg-white p-2 text-slate-700 shadow-lg"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
