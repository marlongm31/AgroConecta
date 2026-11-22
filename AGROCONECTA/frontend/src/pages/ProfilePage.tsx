import { KeyRound, Save, UserCircle } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/Display';
import { PasswordField, TextField } from '../components/ui/FormFields';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import { ROLE_LABELS } from '../utils/labels';
import { formatDateTime } from '../utils/format';
import { userService } from '../services/userService';

export function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const toast = useToast();

  const [profile, setProfile] = useState({
    fullName: user?.fullName ?? '',
    phone: user?.phone ?? '',
    location: user?.location ?? '',
  });
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '' });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  const handleProfile = async (event: FormEvent) => {
    event.preventDefault();
    setSavingProfile(true);
    try {
      await userService.updateProfile(profile);
      await refreshUser();
      toast.success('Perfil actualizado correctamente');
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setSavingProfile(false);
    }
  };

  const handlePassword = async (event: FormEvent) => {
    event.preventDefault();
    setSavingPassword(true);
    try {
      await userService.changePassword(passwords);
      setPasswords({ currentPassword: '', newPassword: '' });
      toast.success('Contraseña actualizada correctamente');
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <>
      <PageHeader title="Mi perfil" subtitle="Actualiza tus datos personales y tu contraseña." />

      <div className="grid gap-6 lg:grid-cols-3">
        <section className="card p-5 lg:col-span-1">
          <div className="flex items-center gap-3">
            <span className="flex h-14 w-14 items-center justify-center rounded-full bg-agro-100 text-xl font-bold text-agro-700">
              {user?.fullName.charAt(0).toUpperCase()}
            </span>
            <div>
              <p className="font-semibold text-slate-900">{user?.fullName}</p>
              <p className="text-sm text-slate-500">{user ? ROLE_LABELS[user.role] : ''}</p>
            </div>
          </div>
          <dl className="mt-5 space-y-3 text-sm">
            <div>
              <dt className="text-slate-400">Correo</dt>
              <dd className="text-slate-700">{user?.email}</dd>
            </div>
            <div>
              <dt className="text-slate-400">Teléfono</dt>
              <dd className="text-slate-700">{user?.phone || '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-400">Ubicación</dt>
              <dd className="text-slate-700">{user?.location || '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-400">Miembro desde</dt>
              <dd className="text-slate-700">{formatDateTime(user?.createdAt)}</dd>
            </div>
          </dl>
        </section>

        <div className="space-y-6 lg:col-span-2">
          <section className="card p-5">
            <h2 className="mb-4 flex items-center gap-2 text-base font-semibold text-slate-900">
              <UserCircle className="h-5 w-5 text-agro-600" /> Datos personales
            </h2>
            <form className="space-y-4" onSubmit={handleProfile} noValidate>
              <TextField
                label="Nombre completo"
                name="fullName"
                required
                value={profile.fullName}
                onChange={(event) => setProfile({ ...profile, fullName: event.target.value })}
              />
              <div className="grid gap-4 sm:grid-cols-2">
                <TextField
                  label="Teléfono"
                  name="phone"
                  value={profile.phone ?? ''}
                  onChange={(event) => setProfile({ ...profile, phone: event.target.value })}
                />
                <TextField
                  label="Ubicación"
                  name="location"
                  value={profile.location ?? ''}
                  onChange={(event) => setProfile({ ...profile, location: event.target.value })}
                />
              </div>
              <div className="flex justify-end">
                <Button type="submit" loading={savingProfile} icon={<Save className="h-4 w-4" />}>
                  Guardar cambios
                </Button>
              </div>
            </form>
          </section>

          <section className="card p-5">
            <h2 className="mb-4 flex items-center gap-2 text-base font-semibold text-slate-900">
              <KeyRound className="h-5 w-5 text-agro-600" /> Cambiar contraseña
            </h2>
            <form className="space-y-4" onSubmit={handlePassword} noValidate>
              <PasswordField
                label="Contraseña actual"
                name="currentPassword"
                required
                value={passwords.currentPassword}
                onChange={(event) => setPasswords({ ...passwords, currentPassword: event.target.value })}
              />
              <PasswordField
                label="Nueva contraseña"
                name="newPassword"
                required
                hint="Mínimo 8 caracteres, combinando letras y números."
                value={passwords.newPassword}
                onChange={(event) => setPasswords({ ...passwords, newPassword: event.target.value })}
              />
              <div className="flex justify-end">
                <Button type="submit" loading={savingPassword}>
                  Actualizar contraseña
                </Button>
              </div>
            </form>
          </section>
        </div>
      </div>
    </>
  );
}
