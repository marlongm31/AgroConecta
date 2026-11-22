import { render, type RenderOptions } from '@testing-library/react';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';
import type { ReactElement, ReactNode } from 'react';
import { AuthProvider } from '../context/AuthContext';
import { OfflineProvider } from '../context/OfflineContext';
import { UnreadMessagesProvider } from '../context/UnreadMessagesContext';
import { ToastProvider } from '../context/ToastContext';

interface ProvidersOptions extends Omit<RenderOptions, 'wrapper'> {
  routerProps?: MemoryRouterProps;
}

export function renderWithProviders(ui: ReactElement, options: ProvidersOptions = {}) {
  const { routerProps, ...renderOptions } = options;

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <MemoryRouter {...routerProps}>
        <ToastProvider>
          <AuthProvider>
            <OfflineProvider>
              <UnreadMessagesProvider>{children}</UnreadMessagesProvider>
            </OfflineProvider>
          </AuthProvider>
        </ToastProvider>
      </MemoryRouter>
    );
  }

  return render(ui, { wrapper: Wrapper, ...renderOptions });
}
