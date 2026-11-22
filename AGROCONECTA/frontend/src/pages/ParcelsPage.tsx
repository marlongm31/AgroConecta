import { Leaf, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { TextAreaField, TextField } from '../components/ui/FormFields';
import { parcelService } from '../services/parcelService';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import { formatNumber } from '../utils/format';
import type { Parcel, ParcelRequest } from '../types';

const EMPTY_FORM: ParcelRequest = { name: '', location: '', area: 0, description: '' };

export function ParcelsPage() {
  const toast = useToast();
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Parcel | null>(null);
  const [toDelete, setToDelete] = useState<Parcel | null>(null);
  const [form, setForm] = useState<ParcelRequest>(EMPTY_FORM);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const page = await parcelService.list();
      setParcels(page.content);
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

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setError('');
    setModalOpen(true);
  };

  const openEdit = (parcel: Parcel) => {
    setEditing(parcel);
    setForm({
      name: parcel.name,
      location: parcel.location,
      area: parcel.area,
      description: parcel.description ?? '',
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editing) {
        await parcelService.update(editing.id, form);
        toast.success('Parcela actualizada correctamente');
      } else {
        await parcelService.create(form);
        toast.success('Parcela creada correctamente');
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
      await parcelService.remove(toDelete.id);
      toast.success('Parcela eliminada correctamente');
      setToDelete(null);
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Parcelas"
        subtitle="Administra las parcelas o lotes de tu finca."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate}>
            Nueva parcela
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : parcels.length === 0 ? (
        <EmptyState
          icon={<Leaf className="h-8 w-8" />}
          title="Aún no tienes parcelas"
          description="Crea tu primera parcela para comenzar a registrar cultivos y gastos."
          action={
            <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate}>
              Crear parcela
            </Button>
          }
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {parcels.map((parcel) => (
            <article key={parcel.id} className="card flex flex-col p-5">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-base font-semibold text-slate-900">{parcel.name}</h3>
                  <p className="text-xs text-slate-400">{parcel.location}</p>
                </div>
                <span className="badge bg-agro-100 text-agro-800">{formatNumber(parcel.area)} ha</span>
              </div>
              <p className="mt-3 flex-1 text-sm text-slate-500">{parcel.description || 'Sin descripción.'}</p>
              <div className="mt-4 flex justify-end gap-2">
                <Button variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => openEdit(parcel)}>
                  Editar
                </Button>
                <Button variant="ghost" className="text-red-600 hover:bg-red-50" icon={<Trash2 className="h-4 w-4" />} onClick={() => setToDelete(parcel)}>
                  Eliminar
                </Button>
              </div>
            </article>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        title={editing ? 'Editar parcela' : 'Nueva parcela'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="parcel-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Crear parcela'}
            </Button>
          </>
        }
      >
        <form id="parcel-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <TextField
            label="Nombre"
            name="name"
            required
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            placeholder="Finca La Esperanza"
          />
          <TextField
            label="Ubicación"
            name="location"
            required
            value={form.location}
            onChange={(event) => setForm({ ...form, location: event.target.value })}
            placeholder="Vereda Los Venados, Valledupar"
          />
          <TextField
            label="Área (hectáreas)"
            name="area"
            type="number"
            step="0.01"
            min="0.01"
            required
            value={form.area || ''}
            onChange={(event) => setForm({ ...form, area: Number(event.target.value) })}
            placeholder="3.50"
          />
          <TextAreaField
            label="Descripción"
            name="description"
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
            placeholder="Riego por goteo, cultivos de ciclo corto..."
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar parcela"
        message={`¿Seguro que deseas eliminar "${toDelete?.name}"? También se eliminarán sus cultivos y registros asociados.`}
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
