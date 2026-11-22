import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { UnreadMessagesContext } from './unreadMessagesStore';
import { useAuth } from './AuthContext';
import { useOffline } from './OfflineContext';
import { chatService } from '../services/chatService';
import { canUseChat } from '../utils/labels';

export function UnreadMessagesProvider({ children }: { children: ReactNode }) {
  const { user, initializing } = useAuth();
  const { online } = useOffline();
  const [count, setCount] = useState(0);
  const userId = user?.id;
  const chatParticipant = canUseChat(user?.role);

  const refresh = useCallback(async () => {
    if (!userId || !chatParticipant || !online) return;
    try {
      setCount(await chatService.unreadCount());
    } catch {
      return;
    }
  }, [chatParticipant, online, userId]);

  useEffect(() => {
    if (!userId || !chatParticipant || initializing) {
      setCount(0);
      return undefined;
    }
    if (!online) return undefined;

    void refresh();
    const timer = window.setInterval(() => {
      void refresh();
    }, 15000);
    const handleFocus = () => {
      void refresh();
    };
    window.addEventListener('focus', handleFocus);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener('focus', handleFocus);
    };
  }, [chatParticipant, initializing, online, refresh, userId]);

  return <UnreadMessagesContext.Provider value={{ count, refresh }}>{children}</UnreadMessagesContext.Provider>;
}
