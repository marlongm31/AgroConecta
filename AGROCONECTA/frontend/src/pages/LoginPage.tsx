import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import { AuthShell } from './AuthShell';
import { Button } from '../components/ui/Button';
import { TextField } from '../components/ui/FormFields';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';

interface LocationState {
  from?: string;
}

export function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const user = await login({ email, password });
      toast.success(`Bienvenido, ${user.fullName.split(' ')[0]}`);
      const from = (location.state as LocationState | null)?.from;
      navigate(from && from !== '/iniciar-sesion' ? from : '/', { replace: true });
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Iniciar sesión"
      subtitle="Ingresa con tu correo y contraseña para continuar."
      footer={
        <>
          ¿No tienes cuenta?{' '}
          <Link to="/registro" className="font-semibold text-agro-700 hover:underline">
            Regístrate aquí
          </Link>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit} noValidate>
        {error ? (
          <div role="alert" className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            <AlertCircle className="h-4 w-4" />
            {error}
          </div>
        ) : null}

        <TextField
          label="Correo electrónico"
          name="email"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="tucorreo@agroconecta.co"
        />
        <TextField
          label="Contraseña"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="••••••••"
        />

        <Button type="submit" loading={loading} className="w-full">
          Iniciar sesión
        </Button>

        <div className="rounded-lg border border-dashed border-agro-200 bg-agro-50/60 px-3 py-2 text-xs text-slate-600">
          <p className="font-semibold text-agro-800">Cuentas de demostración</p>
          <p>Productor: maria@agroconecta.co · Comprador: carlos@agroconecta.co</p>
          <p>Contraseña: Agroconecta123</p>
        </div>
      </form>
    </AuthShell>
  );
}
