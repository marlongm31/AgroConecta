import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import { AuthShell } from './AuthShell';
import { Button } from '../components/ui/Button';
import { SelectField, TextField } from '../components/ui/FormFields';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage, getFieldErrors } from '../lib/errors';
import { ROLE_OPTIONS } from '../utils/labels';
import type { Role } from '../types';

export function RegisterPage() {
  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    role: 'PRODUCTOR' as Role,
    phone: '',
    location: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const update = (field: keyof typeof form) => (value: string) =>
    setForm((current) => ({ ...current, [field]: value }));

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    setErrors({});
    try {
      const user = await register({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        role: form.role,
        phone: form.phone || undefined,
        location: form.location || undefined,
      });
      toast.success(`Cuenta creada. ¡Bienvenido, ${user.fullName.split(' ')[0]}!`);
      navigate('/', { replace: true });
    } catch (caught) {
      setErrors(getFieldErrors(caught));
      setError(getErrorMessage(caught));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Crear cuenta"
      subtitle="Regístrate como productor o comprador en menos de un minuto."
      footer={
        <>
          ¿Ya tienes cuenta?{' '}
          <Link to="/iniciar-sesion" className="font-semibold text-agro-700 hover:underline">
            Inicia sesión
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
          label="Nombre completo"
          name="fullName"
          required
          value={form.fullName}
          error={errors.fullName}
          onChange={(event) => update('fullName')(event.target.value)}
          placeholder="María Fernanda Gómez"
        />
        <TextField
          label="Correo electrónico"
          name="email"
          type="email"
          required
          value={form.email}
          error={errors.email}
          onChange={(event) => update('email')(event.target.value)}
          placeholder="tucorreo@agroconecta.co"
        />
        <TextField
          label="Contraseña"
          name="password"
          type="password"
          required
          value={form.password}
          error={errors.password}
          onChange={(event) => update('password')(event.target.value)}
          placeholder="Mínimo 8 caracteres"
          hint="Debe combinar letras y números."
        />
        <SelectField
          label="Rol"
          name="role"
          value={form.role}
          options={ROLE_OPTIONS}
          error={errors.role}
          onChange={(event) => update('role')(event.target.value)}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <TextField
            label="Teléfono (opcional)"
            name="phone"
            value={form.phone}
            error={errors.phone}
            onChange={(event) => update('phone')(event.target.value)}
            placeholder="+57 300 000 0000"
          />
          <TextField
            label="Ubicación (opcional)"
            name="location"
            value={form.location}
            error={errors.location}
            onChange={(event) => update('location')(event.target.value)}
            placeholder="Valledupar, Cesar"
          />
        </div>

        <Button type="submit" loading={loading} className="w-full">
          Crear cuenta
        </Button>
      </form>
    </AuthShell>
  );
}
