import { useContext } from 'react';
import { UnreadMessagesContext } from './unreadMessagesStore';

export function useUnreadMessages() {
  const context = useContext(UnreadMessagesContext);
  if (!context) {
    throw new Error('useUnreadMessages debe usarse dentro de UnreadMessagesProvider');
  }
  return context;
}
