import { Bug, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader, Badge } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { SelectField, TextAreaField, TextField } from '../components/ui/FormFields';
import { activityService } from '../services/activityService';
import { cropService } from '../services/cropService';
import { useToast } from '../context/ToastContext';
import { useOffline } from '../context/OfflineContext';
import { getErrorMessage } from '../lib/errors';
import { ACTIVITY_TYPE_LABELS, ACTIVITY_TYPE_OPTIONS } from '../utils/labels';
import { formatDate, todayISO } from '../utils/format';
import type { ActivityRequest, ActivityType, AgriculturalActivity, Crop } from '../types';

const EMPTY_FORM: ActivityRequest = {
  cropId: 0,
  type: 'RIEGO',
  activityDate: todayISO(),
  description: '',
  observations: '',
};

export function ActivitiesPage() {
  const toast = useToast();
  const { online, enqueue, operations } = useOffline();
  const [activities, setActivities] = useState<AgriculturalActivity[]>([]);
  const [crops, setCrops] = useState<Crop[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AgriculturalActivity | null>(null);
  const [toDelete, setToDelete] = useState<AgriculturalActivity | null>(null);
  const [form, setForm] = useState<ActivityRequest>(EMPTY_FORM);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [activityPage, cropPage] = await Promise.all([activityService.list(), cropService.list()]);
      setActivities(activityPage.content);
      setCrops(cropPage.content);
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

  const pendingActivities: AgriculturalActivity[] = operations
    .filter((operation) => operation.resource === 'activity' && operation.action === 'create')
    .map((operation, index) => ({
      id: -(index + 1),
      ...(operation.payload as ActivityRequest),
      createdAt: operation.createdAt,
      updatedAt: operation.createdAt,
    }));
  const visibleActivities = [...activities, ...pendingActivities];

  const cropName = (cropId: number) => crops.find((crop) => crop.id === cropId)?.name ?? `#${cropId}`;

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, cropId: crops[0]?.id ?? 0 });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (activity: AgriculturalActivity) => {
    setEditing(activity);
    setForm({
      cropId: activity.cropId,
      type: activity.type,
      activityDate: activity.activityDate,
      description: activity.description,
      observations: activity.observations ?? '',
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (!online) {
        enqueue('activity', editing ? 'update' : 'create', form, editing?.id);
        toast.info('Actividad guardada. Se sincronizará cuando vuelva la conexión.');
        setModalOpen(false);
        return;
      }
      if (editing) {
        await activityService.update(editing.id, form);
        toast.success('Actividad actualizada correctamente');
      } else {
        await activityService.create(form);
        toast.success('Actividad registrada correctamente');
      }
      setModalOpen(false);
      await load();
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!toDelete) return;
    setSaving(true);
    try {
      if (!online) {
        enqueue('activity', 'delete', {}, toDelete.id);
        setActivities((current) => current.filter((item) => item.id !== toDelete.id));
        setToDelete(null);
        toast.info('Eliminación guardada. Se sincronizará cuando vuelva la conexión.');
        return;
      }
      await activityService.remove(toDelete.id);
      toast.success('Actividad eliminada correctamente');
      setToDelete(null);
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const noCrops = !loading && crops.length === 0;

  return (
    <>
      <PageHeader
        title="Actividades agrícolas"
        subtitle="Registra siembras, riegos, fertilizaciones, podas y cosechas."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate} disabled={noCrops}>
            Nueva actividad
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : noCrops ? (
        <EmptyState icon={<Bug className="h-8 w-8" />} title="Primero registra un cultivo" description="Las actividades se asocian a un cultivo." />
      ) : visibleActivities.length === 0 ? (
        <EmptyState icon={<Bug className="h-8 w-8" />} title="Aún no hay actividades" description="Registra la primera actividad de tus cultivos." />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Tipo</th>
                <th>Cultivo</th>
                <th>Descripción</th>
                <th className="text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleActivities.map((activity) => (
                <tr key={activity.id}>
                  <td>{formatDate(activity.activityDate)}</td>
                  <td>
                    <Badge tone="green">{ACTIVITY_TYPE_LABELS[activity.type]}</Badge>
                  </td>
                  <td>{cropName(activity.cropId)}</td>
                  <td className="max-w-xs truncate">
                    {activity.description}
                  </td>
                  <td>
                    <div className="flex justify-end gap-1">
                      {activity.id > 0 ? (
                        <>
                          <Button variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => openEdit(activity)}>
                            Editar
                          </Button>
                          <Button variant="ghost" className="text-red-600 hover:bg-red-50" icon={<Trash2 className="h-4 w-4" />} onClick={() => setToDelete(activity)}>
                            Eliminar
                          </Button>
                        </>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={modalOpen}
        title={editing ? 'Editar actividad' : 'Nueva actividad'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="activity-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Registrar actividad'}
            </Button>
          </>
        }
      >
        <form id="activity-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <SelectField
            label="Cultivo"
            name="cropId"
            required
            value={String(form.cropId)}
            onChange={(event) => setForm({ ...form, cropId: Number(event.target.value) })}
            options={crops.map((crop) => ({ value: String(crop.id), label: crop.name }))}
            placeholder="Selecciona un cultivo"
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Tipo de actividad"
              name="type"
              required
              value={form.type}
              onChange={(event) => setForm({ ...form, type: event.target.value as ActivityType })}
              options={ACTIVITY_TYPE_OPTIONS}
            />
            <TextField
              label="Fecha"
              name="activityDate"
              type="date"
              required
              value={form.activityDate}
              onChange={(event) => setForm({ ...form, activityDate: event.target.value })}
            />
          </div>
          <TextField
            label="Descripción"
            name="description"
            required
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
            placeholder="Riego por goteo de dos horas"
          />
          <TextAreaField
            label="Observaciones"
            name="observations"
            value={form.observations}
            onChange={(event) => setForm({ ...form, observations: event.target.value })}
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar actividad"
        message="¿Seguro que deseas eliminar esta actividad?"
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
