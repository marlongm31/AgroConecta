import { describe, expect, it, vi, type Mock } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AxiosError } from 'axios';
import { LoginPage } from './LoginPage';
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

const authMock = authService as unknown as { login: Mock };

const user: User = {
  id: 2,
  fullName: 'María Gómez',
  email: 'maria@agroconecta.co',
  role: 'PRODUCTOR',
  active: true,
};

function renderLogin() {
  return renderWithProviders(<LoginPage />, {
    routerProps: { initialEntries: ['/iniciar-sesion'] },
  });
}

describe('LoginPage', () => {
  it('renders the login form and the demo credentials hint', () => {
    renderLogin();
    expect(screen.getByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument();
    expect(screen.getByLabelText('Correo electrónico')).toBeInTheDocument();
    expect(screen.getByText(/Cuentas de demostración/i)).toBeInTheDocument();
  });

  it('authenticates with the provided credentials', async () => {
    authMock.login.mockResolvedValue({ token: 'jwt-token', user });
    const user1 = userEvent.setup();
    renderLogin();

    await user1.type(screen.getByLabelText('Correo electrónico'), 'maria@agroconecta.co');
    await user1.type(screen.getByLabelText('Contraseña'), 'Agroconecta123');
    await user1.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    await waitFor(() =>
      expect(authMock.login).toHaveBeenCalledWith({
        email: 'maria@agroconecta.co',
        password: 'Agroconecta123',
      }),
    );
  });

  it('shows an error message when the credentials are rejected', async () => {
    const error = new AxiosError('Unauthorized');
    error.response = {
      status: 401,
      data: { success: false, message: 'Credenciales inválidas' },
      statusText: '',
      headers: {},
      config: {} as never,
    };
    authMock.login.mockRejectedValue(error);
    const user1 = userEvent.setup();
    renderLogin();

    await user1.type(screen.getByLabelText('Correo electrónico'), 'maria@agroconecta.co');
    await user1.type(screen.getByLabelText('Contraseña'), 'incorrecta');
    await user1.click(screen.getByRole('button', { name: 'Iniciar sesión' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales inválidas');
  });
});
