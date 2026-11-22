import { describe, expect, it, vi, type Mock } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { renderWithProviders } from '../../test/utils';
import { storage } from '../../lib/storage';
import { userService } from '../../services/userService';
import type { User } from '../../types';

vi.mock('../../services/userService', () => ({
  userService: {
    me: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
    list: vi.fn(),
    updateStatus: vi.fn(),
    updateRole: vi.fn(),
  },
}));

const userMock = userService as unknown as { me: Mock };

const producer: User = {
  id: 2,
  fullName: 'María Gómez',
  email: 'maria@agroconecta.co',
  role: 'PRODUCTOR',
  active: true,
};

function renderGuarded(roles?: User['role'][]) {
  return renderWithProviders(
    <Routes>
      <Route path="/" element={<div>Panel principal</div>} />
      <Route path="/iniciar-sesion" element={<div>Formulario de acceso</div>} />
      <Route
        path="/protegida"
        element={
          <ProtectedRoute roles={roles}>
            <div>Contenido protegido</div>
          </ProtectedRoute>
        }
      />
    </Routes>,
    { routerProps: { initialEntries: ['/protegida'] } },
  );
}

describe('ProtectedRoute', () => {
  it('redirects unauthenticated visitors to the login page', async () => {
    renderGuarded();
    expect(await screen.findByText('Formulario de acceso')).toBeInTheDocument();
    expect(screen.queryByText('Contenido protegido')).not.toBeInTheDocument();
  });

  it('renders the content for authenticated users with the right role', async () => {
    storage.setToken('jwt-token');
    storage.setUser(producer);
    userMock.me.mockResolvedValue(producer);

    renderGuarded(['PRODUCTOR']);

    expect(await screen.findByText('Contenido protegido')).toBeInTheDocument();
  });

  it('redirects authenticated users that lack the required role', async () => {
    storage.setToken('jwt-token');
    storage.setUser({ ...producer, role: 'COMPRADOR' });
    userMock.me.mockResolvedValue({ ...producer, role: 'COMPRADOR' });

    renderGuarded(['PRODUCTOR']);

    await waitFor(() => expect(screen.getByText('Panel principal')).toBeInTheDocument());
    expect(screen.queryByText('Contenido protegido')).not.toBeInTheDocument();
  });
});
