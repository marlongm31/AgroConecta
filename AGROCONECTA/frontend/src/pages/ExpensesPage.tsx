import { Coins, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { SelectField, TextField } from '../components/ui/FormFields';
import { expenseService } from '../services/expenseService';
import { cropService } from '../services/cropService';
import { parcelService } from '../services/parcelService';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import { formatCurrency, formatDate, todayISO } from '../utils/format';
import type { Crop, Expense, ExpenseRequest, Parcel } from '../types';

const EMPTY_FORM = {
  parcelId: '',
  cropId: '',
  concept: '',
  amount: '',
  expenseDate: todayISO(),
};

export function ExpensesPage() {
  const toast = useToast();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [crops, setCrops] = useState<Crop[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Expense | null>(null);
  const [toDelete, setToDelete] = useState<Expense | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const [expensePage, parcelPage, cropPage] = await Promise.all([
        expenseService.list(),
        parcelService.list(),
        cropService.list(),
      ]);
      setExpenses(expensePage.content);
      setParcels(parcelPage.content);
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

  const total = useMemo(
    () => expenses.reduce((sum, expense) => sum + Number(expense.amount), 0),
    [expenses],
  );

  const relationLabel = (expense: Expense) => {
    if (expense.cropId) {
      return `Cultivo: ${crops.find((crop) => crop.id === expense.cropId)?.name ?? `#${expense.cropId}`}`;
    }
    if (expense.parcelId) {
      return `Parcela: ${parcels.find((parcel) => parcel.id === expense.parcelId)?.name ?? `#${expense.parcelId}`}`;
    }
    return '—';
  };

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setError('');
    setModalOpen(true);
  };

  const openEdit = (expense: Expense) => {
    setEditing(expense);
    setForm({
      parcelId: expense.parcelId ? String(expense.parcelId) : '',
      cropId: expense.cropId ? String(expense.cropId) : '',
      concept: expense.concept,
      amount: String(expense.amount),
      expenseDate: expense.expenseDate,
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload: ExpenseRequest = {
      parcelId: form.parcelId ? Number(form.parcelId) : null,
      cropId: form.cropId ? Number(form.cropId) : null,
      concept: form.concept,
      amount: Number(form.amount),
      expenseDate: form.expenseDate,
    };
    try {
      if (editing) {
        await expenseService.update(editing.id, payload);
        toast.success('Gasto actualizado correctamente');
      } else {
        await expenseService.create(payload);
        toast.success('Gasto registrado correctamente');
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
      await expenseService.remove(toDelete.id);
      toast.success('Gasto eliminado correctamente');
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
        title="Gastos"
        subtitle="Lleva el control de los costos de tu operación agrícola."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate}>
            Registrar gasto
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : expenses.length === 0 ? (
        <EmptyState icon={<Coins className="h-8 w-8" />} title="Aún no hay gastos registrados" description="Registra tus primeros costos de producción." />
      ) : (
        <>
          <div className="mb-4 flex items-center justify-between rounded-2xl border border-agro-100 bg-agro-50 px-5 py-4">
            <span className="text-sm font-medium text-agro-800">Total de gastos</span>
            <span className="text-xl font-bold text-agro-800">{formatCurrency(total)}</span>
          </div>
          <div className="card overflow-x-auto">
            <table className="table-base">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Concepto</th>
                  <th>Relación</th>
                  <th className="text-right">Valor</th>
                  <th className="text-right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {expenses.map((expense) => (
                  <tr key={expense.id}>
                    <td>{formatDate(expense.expenseDate)}</td>
                    <td className="font-medium text-slate-800">{expense.concept}</td>
                    <td className="text-slate-500">{relationLabel(expense)}</td>
                    <td className="text-right font-semibold text-slate-800">{formatCurrency(expense.amount)}</td>
                    <td>
                      <div className="flex justify-end gap-1">
                        <Button variant="ghost" icon={<Pencil className="h-4 w-4" />} onClick={() => openEdit(expense)}>
                          Editar
                        </Button>
                        <Button variant="ghost" className="text-red-600 hover:bg-red-50" icon={<Trash2 className="h-4 w-4" />} onClick={() => setToDelete(expense)}>
                          Eliminar
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <Modal
        open={modalOpen}
        title={editing ? 'Editar gasto' : 'Registrar gasto'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="expense-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Registrar gasto'}
            </Button>
          </>
        }
      >
        <form id="expense-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <TextField
            label="Concepto"
            name="concept"
            required
            value={form.concept}
            onChange={(event) => setForm({ ...form, concept: event.target.value })}
            placeholder="Fertilizante NPK 15-15-15"
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField
              label="Valor (COP)"
              name="amount"
              type="number"
              step="0.01"
              min="0.01"
              required
              value={form.amount}
              onChange={(event) => setForm({ ...form, amount: event.target.value })}
              placeholder="185000"
            />
            <TextField
              label="Fecha"
              name="expenseDate"
              type="date"
              required
              value={form.expenseDate}
              onChange={(event) => setForm({ ...form, expenseDate: event.target.value })}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Parcela (opcional)"
              name="parcelId"
              value={form.parcelId}
              onChange={(event) => setForm({ ...form, parcelId: event.target.value })}
              options={parcels.map((parcel) => ({ value: String(parcel.id), label: parcel.name }))}
              placeholder="Sin parcela"
            />
            <SelectField
              label="Cultivo (opcional)"
              name="cropId"
              value={form.cropId}
              onChange={(event) => setForm({ ...form, cropId: event.target.value })}
              options={crops.map((crop) => ({ value: String(crop.id), label: crop.name }))}
              placeholder="Sin cultivo"
            />
          </div>
          <p className="text-xs text-slate-500">Debes asociar el gasto al menos a una parcela o a un cultivo.</p>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar gasto"
        message={`¿Seguro que deseas eliminar "${toDelete?.concept}"?`}
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
