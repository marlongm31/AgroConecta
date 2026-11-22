import { BarChart3, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { SelectField, TextAreaField, TextField } from '../components/ui/FormFields';
import { productionService } from '../services/productionService';
import { cropService } from '../services/cropService';
import { useToast } from '../context/ToastContext';
import { useOffline } from '../context/OfflineContext';
import { getErrorMessage } from '../lib/errors';
import { MEASUREMENT_UNIT_LABELS, MEASUREMENT_UNIT_OPTIONS } from '../utils/labels';
import { formatDate, formatNumber, todayISO } from '../utils/format';
import type { Crop, MeasurementUnit, ProductionRecord, ProductionRequest } from '../types';

const EMPTY_FORM = {
  cropId: '',
  quantity: '',
  unit: 'KILOGRAMO' as MeasurementUnit,
  harvestDate: todayISO(),
  observations: '',
};

export function ProductionPage() {
  const toast = useToast();
  const { online, enqueue, operations } = useOffline();
  const [records, setRecords] = useState<ProductionRecord[]>([]);
  const [crops, setCrops] = useState<Crop[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ProductionRecord | null>(null);
  const [toDelete, setToDelete] = useState<ProductionRecord | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [recordPage, cropPage] = await Promise.all([productionService.list(), cropService.list()]);
      setRecords(recordPage.content);
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

  const pendingRecords: ProductionRecord[] = operations
    .filter((operation) => operation.resource === 'production' && operation.action === 'create')
    .map((operation, index) => ({
      id: -(index + 1),
      ...(operation.payload as ProductionRequest),
      createdAt: operation.createdAt,
      updatedAt: operation.createdAt,
    }));
  const visibleRecords = [...records, ...pendingRecords];

  const cropName = (cropId: number) => crops.find((crop) => crop.id === cropId)?.name ?? `#${cropId}`;

  const openCreate = () => {
    setEditing(null);
    setForm({ ...EMPTY_FORM, cropId: crops[0] ? String(crops[0].id) : '' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (record: ProductionRecord) => {
    setEditing(record);
    setForm({
      cropId: String(record.cropId),
      quantity: String(record.quantity),
      unit: record.unit,
      harvestDate: record.harvestDate,
      observations: record.observations ?? '',
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload: ProductionRequest = {
      cropId: Number(form.cropId),
      quantity: Number(form.quantity),
      unit: form.unit,
      harvestDate: form.harvestDate,
      observations: form.observations,
    };
    try {
      if (!online) {
        enqueue('production', editing ? 'update' : 'create', payload, editing?.id);
        toast.info('Cosecha guardada. Se sincronizará cuando vuelva la conexión.');
        setModalOpen(false);
        return;
      }
      if (editing) {
        await productionService.update(editing.id, payload);
        toast.success('Registro de producción actualizado');
      } else {
        await productionService.create(payload);
        toast.success('Producción registrada correctamente');
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
        enqueue('production', 'delete', {}, toDelete.id);
        setRecords((current) => current.filter((item) => item.id !== toDelete.id));
        setToDelete(null);
        toast.info('Eliminación guardada. Se sincronizará cuando vuelva la conexión.');
        return;
      }
      await productionService.remove(toDelete.id);
      toast.success('Registro eliminado correctamente');
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
        title="Producción"
        subtitle="Registra las cosechas obtenidas por cultivo."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate} disabled={noCrops}>
            Registrar producción
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : noCrops ? (
        <EmptyState icon={<BarChart3 className="h-8 w-8" />} title="Primero registra un cultivo" description="La producción se asocia a un cultivo." />
      ) : visibleRecords.length === 0 ? (
        <EmptyState icon={<BarChart3 className="h-8 w-8" />} title="Aún no hay registros de producción" description="Registra tu primera cosecha." />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table-base">
            <thead>
              <tr>
                <th>Fecha de cosecha</th>
                <th>Cultivo</th>
                <th className="text-right">Cantidad</th>
                <th>Observaciones</th>
                <th className="text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleRecords.map((record) => (
                <tr key={record.id}>
                  <td>{formatDate(record.harvestDate)}</td>
                  <td className="font-medium text-slate-800">{cropName(record.cropId)}</td>
                  <td className="text-right">
                    {formatNumber(record.quantity)} {MEASUREMENT_UNIT_LABELS[record.unit].toLowerCase()}
                  </td>
                  <td className="max-w-xs truncate text-slate-500">
                    {record.observations || '—'}
                  </td>
                  <td>
                    <div className="flex justify-end gap-1">
                      {record.id > 0 ? (
                        <>
                          <Button variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => openEdit(record)}>
                            Editar
                          </Button>
                          <Button variant="ghost" className="text-red-600 hover:bg-red-50" icon={<Trash2 className="h-4 w-4" />} onClick={() => setToDelete(record)}>
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
        title={editing ? 'Editar producción' : 'Registrar producción'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="production-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Registrar'}
            </Button>
          </>
        }
      >
        <form id="production-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <SelectField
            label="Cultivo"
            name="cropId"
            required
            value={form.cropId}
            onChange={(event) => setForm({ ...form, cropId: event.target.value })}
            options={crops.map((crop) => ({ value: String(crop.id), label: crop.name }))}
            placeholder="Selecciona un cultivo"
          />
          <div className="grid gap-4 sm:grid-cols-3">
            <TextField
              label="Cantidad"
              name="quantity"
              type="number"
              step="0.01"
              min="0.01"
              required
              value={form.quantity}
              onChange={(event) => setForm({ ...form, quantity: event.target.value })}
              placeholder="850"
            />
            <SelectField
              label="Unidad"
              name="unit"
              required
              value={form.unit}
              onChange={(event) => setForm({ ...form, unit: event.target.value as MeasurementUnit })}
              options={MEASUREMENT_UNIT_OPTIONS}
            />
            <TextField
              label="Fecha de cosecha"
              name="harvestDate"
              type="date"
              required
              value={form.harvestDate}
              onChange={(event) => setForm({ ...form, harvestDate: event.target.value })}
            />
          </div>
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
        title="Eliminar registro"
        message="¿Seguro que deseas eliminar este registro de producción?"
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
