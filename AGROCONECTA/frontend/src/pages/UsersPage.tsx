import { useEffect, useState } from 'react';
import { PageHeader, Badge } from '../components/ui/Display';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Button } from '../components/ui/Button';
import { userService } from '../services/userService';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import { ROLE_LABELS, ROLE_OPTIONS } from '../utils/labels';
import { formatDate } from '../utils/format';
import type { Role, User } from '../types';

export function UsersPage() {
  const toast = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const page = await userService.list(0, 100);
      setUsers(page.content);
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const toggleStatus = async (user: User) => {
    try {
      await userService.updateStatus(user.id, !user.active);
      toast.success('Estado del usuario actualizado');
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    }
  };

  const changeRole = async (user: User, role: Role) => {
    try {
      await userService.updateRole(user.id, role);
      toast.success('Rol actualizado');
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    }
  };

  return (
    <>
      <PageHeader title="Usuarios" subtitle="Administra los roles y el estado de las cuentas." />
      {loading ? (
        <PageLoader />
      ) : users.length === 0 ? (
        <EmptyState title="No hay usuarios registrados" />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Registro</th>
                <th className="text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="font-medium text-slate-800">{user.fullName}</td>
                  <td>{user.email}</td>
                  <td>
                    <select
                      aria-label={`Rol de ${user.fullName}`}
                      value={user.role}
                      onChange={(event) => changeRole(user, event.target.value as Role)}
                      className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-xs"
                    >
                      <option value="PRODUCTOR">{ROLE_LABELS.PRODUCTOR}</option>
                      <option value="COMPRADOR">{ROLE_LABELS.COMPRADOR}</option>
                      <option value="ADMINISTRADOR">{ROLE_LABELS.ADMINISTRADOR}</option>
                    </select>
                  </td>
                  <td>
                    <Badge tone={user.active ? 'green' : 'red'}>{user.active ? 'Activo' : 'Inactivo'}</Badge>
                  </td>
                  <td className="text-slate-500">{formatDate(user.createdAt)}</td>
                  <td>
                    <div className="flex justify-end">
                      <Button variant="ghost" onClick={() => toggleStatus(user)}>
                        {user.active ? 'Desactivar' : 'Activar'}
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <p className="mt-3 text-xs text-slate-400">
        Roles disponibles: {ROLE_OPTIONS.map((option) => option.label).join(', ')} y Administrador.
      </p>
    </>
  );
}
