import { describe, expect, it, vi, type Mock } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RegisterPage } from './RegisterPage';
import { renderWithProviders } from '../test/utils';
import { authService } from '../services/authService';
import type { User } from '../types';

vi.mock('../services/authService', () => ({
  authService: { login: vi.fn(), register: vi.fn(), logout: vi.fn() },
}));
vi.mock('../services/userService', () => ({
  userService: {
    me: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
    list: vi.fn(),
    updateStatus: vi.fn(),
    updateRole: vi.fn(),
  },
}));

const authMock = authService as unknown as { register: Mock };

const createdUser: User = {
  id: 9,
  fullName: 'Pedro Pérez',
  email: 'pedro@agroconecta.co',
  role: 'COMPRADOR',
  active: true,
};

describe('RegisterPage', () => {
  it('renders the registration form without exposing the administrator role', () => {
    renderWithProviders(<RegisterPage />, { routerProps: { initialEntries: ['/registro'] } });
    expect(screen.getByRole('heading', { name: 'Crear cuenta' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Administrador' })).not.toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Productor' })).toBeInTheDocument();
  });

  it('submits the selected role and personal data', async () => {
    authMock.register.mockResolvedValue({ token: 'jwt-token', user: createdUser });
    const user1 = userEvent.setup();
    renderWithProviders(<RegisterPage />, { routerProps: { initialEntries: ['/registro'] } });

    await user1.type(screen.getByLabelText('Nombre completo'), 'Pedro Pérez');
    await user1.type(screen.getByLabelText('Correo electrónico'), 'pedro@agroconecta.co');
    await user1.type(screen.getByLabelText('Contraseña'), 'Clave1234');
    await user1.selectOptions(screen.getByLabelText('Rol'), 'COMPRADOR');
    await user1.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    await waitFor(() =>
      expect(authMock.register).toHaveBeenCalledWith(
        expect.objectContaining({
          fullName: 'Pedro Pérez',
          email: 'pedro@agroconecta.co',
          password: 'Clave1234',
          role: 'COMPRADOR',
        }),
      ),
    );
  });
});
