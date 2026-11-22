import { Pencil, Plus, Sprout, Trash2 } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader, Badge } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { SelectField, TextAreaField, TextField } from '../components/ui/FormFields';
import { cropService } from '../services/cropService';
import { parcelService } from '../services/parcelService';
import { useToast } from '../context/ToastContext';
import { useOffline } from '../context/OfflineContext';
import { getErrorMessage } from '../lib/errors';
import { CROP_STATUS_LABELS, CROP_STATUS_OPTIONS, CROP_STATUS_TONES } from '../utils/labels';
import { formatDate, todayISO } from '../utils/format';
import type { Crop, CropRequest, CropStatus, Parcel } from '../types';

const EMPTY_FORM: CropRequest = {
  parcelId: 0,
  name: '',
  variety: '',
  plantingDate: todayISO(),
  estimatedHarvestDate: '',
  status: 'PLANIFICADO',
  observations: '',
};

export function CropsPage() {
  const toast = useToast();
  const { online, enqueue, operations } = useOffline();
  const [crops, setCrops] = useState<Crop[]>([]);
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Crop | null>(null);
  const [toDelete, setToDelete] = useState<Crop | null>(null);
  const [form, setForm] = useState<CropRequest>(EMPTY_FORM);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [cropPage, parcelPage] = await Promise.all([cropService.list(), parcelService.list()]);
      setCrops(cropPage.content);
      setParcels(parcelPage.content);
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

  const pendingCrops: Crop[] = operations
    .filter((operation) => operation.resource === 'crop' && operation.action === 'create')
    .map((operation, index) => {
      const payload = operation.payload as CropRequest;
      return {
        id: -(index + 1),
        ...payload,
        createdAt: operation.createdAt,
        updatedAt: operation.createdAt,
      };
    });
  const visibleCrops = [...crops, ...pendingCrops];

  const parcelName = (parcelId: number) => parcels.find((parcel) => parcel.id === parcelId)?.name ?? `#${parcelId}`;

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, parcelId: parcels[0]?.id ?? 0 });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (crop: Crop) => {
    setEditing(crop);
    setForm({
      parcelId: crop.parcelId,
      name: crop.name,
      variety: crop.variety ?? '',
      plantingDate: crop.plantingDate,
      estimatedHarvestDate: crop.estimatedHarvestDate ?? '',
      status: crop.status,
      observations: crop.observations ?? '',
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload: CropRequest = {
      ...form,
      estimatedHarvestDate: form.estimatedHarvestDate || null,
    };
    try {
      if (!online) {
        enqueue('crop', editing ? 'update' : 'create', payload, editing?.id);
        toast.info('Cultivo guardado. Se sincronizará cuando vuelva la conexión.');
        setModalOpen(false);
        return;
      }
      if (editing) {
        await cropService.update(editing.id, payload);
        toast.success('Cultivo actualizado correctamente');
      } else {
        await cropService.create(payload);
        toast.success('Cultivo creado correctamente');
      }
      setModalOpen(false);
      await load();
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const handleStatus = async (crop: Crop, status: CropStatus) => {
    try {
      if (!online) {
        enqueue(
          'crop',
          'update',
          {
            parcelId: crop.parcelId,
            name: crop.name,
            variety: crop.variety ?? '',
            plantingDate: crop.plantingDate,
            estimatedHarvestDate: crop.estimatedHarvestDate ?? null,
            status,
            observations: crop.observations ?? '',
          },
          crop.id,
        );
        setCrops((current) => current.map((item) => (item.id === crop.id ? { ...item, status } : item)));
        toast.info('Estado guardado. Se sincronizará cuando vuelva la conexión.');
        return;
      }
      await cropService.changeStatus(crop.id, status);
      toast.success('Estado actualizado');
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    }
  };

  const handleDelete = async () => {
    if (!toDelete) return;
    setSaving(true);
    try {
      if (!online) {
        enqueue('crop', 'delete', {}, toDelete.id);
        setCrops((current) => current.filter((item) => item.id !== toDelete.id));
        setToDelete(null);
        toast.info('Eliminación guardada. Se sincronizará cuando vuelva la conexión.');
        return;
      }
      await cropService.remove(toDelete.id);
      toast.success('Cultivo eliminado correctamente');
      setToDelete(null);
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const noParcels = !loading && parcels.length === 0;

  return (
    <>
      <PageHeader
        title="Cultivos"
        subtitle="Registra y da seguimiento a tus cultivos por parcela."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate} disabled={noParcels}>
            Nuevo cultivo
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : noParcels ? (
        <EmptyState
          icon={<Sprout className="h-8 w-8" />}
          title="Primero crea una parcela"
          description="Necesitas al menos una parcela para registrar cultivos."
        />
      ) : visibleCrops.length === 0 ? (
        <EmptyState
          icon={<Sprout className="h-8 w-8" />}
          title="Aún no tienes cultivos"
          description="Registra tu primer cultivo para llevar su seguimiento."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Cultivo</th>
                <th>Parcela</th>
                <th>Siembra</th>
                <th>Cosecha estimada</th>
                <th>Estado</th>
                <th className="text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleCrops.map((crop) => (
                <tr key={crop.id}>
                  <td>
                    <p className="font-medium text-slate-800">{crop.name}</p>
                    <p className="text-xs text-slate-400">{crop.variety || 'Sin variedad'}</p>
                  </td>
                  <td>{parcelName(crop.parcelId)}</td>
                  <td>{formatDate(crop.plantingDate)}</td>
                  <td>{formatDate(crop.estimatedHarvestDate)}</td>
                  <td>
                    {crop.id > 0 ? (
                      <select
                        aria-label={`Estado de ${crop.name}`}
                        value={crop.status}
                        onChange={(event) => handleStatus(crop, event.target.value as CropStatus)}
                        className="rounded-lg border border-slate-200 bg-white px-2 py-1 text-xs"
                      >
                        {CROP_STATUS_OPTIONS.map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                    ) : null}
                    <div className="mt-1">
                      <Badge tone={CROP_STATUS_TONES[crop.status]}>{CROP_STATUS_LABELS[crop.status]}</Badge>
                    </div>
                  </td>
                  <td>
                    <div className="flex justify-end gap-1">
                      {crop.id > 0 ? (
                        <>
                      <Button variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => openEdit(crop)}>
                        Editar
                      </Button>
                      <Button variant="ghost" className="text-red-600 hover:bg-red-50" icon={<Trash2 className="h-4 w-4" />} onClick={() => setToDelete(crop)}>
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
        title={editing ? 'Editar cultivo' : 'Nuevo cultivo'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="crop-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Crear cultivo'}
            </Button>
          </>
        }
      >
        <form id="crop-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <SelectField
            label="Parcela"
            name="parcelId"
            required
            value={String(form.parcelId)}
            onChange={(event) => setForm({ ...form, parcelId: Number(event.target.value) })}
            options={parcels.map((parcel) => ({ value: String(parcel.id), label: parcel.name }))}
            placeholder="Selecciona una parcela"
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField
              label="Nombre"
              name="name"
              required
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              placeholder="Tomate"
            />
            <TextField
              label="Variedad"
              name="variety"
              value={form.variety}
              onChange={(event) => setForm({ ...form, variety: event.target.value })}
              placeholder="Chonto"
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField
              label="Fecha de siembra"
              name="plantingDate"
              type="date"
              required
              value={form.plantingDate}
              onChange={(event) => setForm({ ...form, plantingDate: event.target.value })}
            />
            <TextField
              label="Cosecha estimada"
              name="estimatedHarvestDate"
              type="date"
              value={form.estimatedHarvestDate ?? ''}
              onChange={(event) => setForm({ ...form, estimatedHarvestDate: event.target.value })}
            />
          </div>
          <SelectField
            label="Estado"
            name="status"
            required
            value={form.status}
            onChange={(event) => setForm({ ...form, status: event.target.value as CropStatus })}
            options={CROP_STATUS_OPTIONS}
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
        title="Eliminar cultivo"
        message={`¿Seguro que deseas eliminar "${toDelete?.name}"?`}
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
