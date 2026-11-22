import { createContext } from 'react';

export interface UnreadMessagesContextValue {
  count: number;
  refresh: () => Promise<void>;
}

export const UnreadMessagesContext = createContext<UnreadMessagesContextValue | undefined>(undefined);
