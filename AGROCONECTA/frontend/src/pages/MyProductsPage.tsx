import { Camera, Eye, EyeOff, Pencil, Plus, Trash2, Wheat } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Button } from '../components/ui/Button';
import { PageHeader, Badge } from '../components/ui/Display';
import { ConfirmDialog } from '../components/ui/ConfirmDialog';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Modal } from '../components/ui/Modal';
import { SelectField, TextAreaField, TextField } from '../components/ui/FormFields';
import { productService } from '../services/productService';
import { ProductImageManager } from '../components/products/ProductImageManager';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import {
  MEASUREMENT_UNIT_OPTIONS,
  PRODUCT_CATEGORY_LABELS,
  PRODUCT_CATEGORY_OPTIONS,
} from '../utils/labels';
import { formatCurrency, formatNumber } from '../utils/format';
import { getProductStatus, PRODUCT_STATUS_META } from '../utils/productStatus';
import { assetUrl } from '../lib/assetUrl';
import type {
  MeasurementUnit,
  Product,
  ProductCategory,
  ProductRequest,
} from '../types';

export function MyProductsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Product | null>(null);
  const [toDelete, setToDelete] = useState<Product | null>(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    name: '',
    description: '',
    characteristics: '',
    category: 'HORTALIZAS' as ProductCategory,
    price: '',
    availableQuantity: '',
    unit: 'KILOGRAMO' as MeasurementUnit,
    contactPhone: '',
    contactEmail: '',
    active: true,
  });

  const load = async () => {
    setLoading(true);
    try {
      const page = await productService.listMine();
      setProducts(page.content);
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
    setForm({
      name: '',
      description: '',
      characteristics: '',
      category: 'HORTALIZAS',
      price: '',
      availableQuantity: '',
      unit: 'KILOGRAMO',
      contactPhone: user?.phone ?? '',
      contactEmail: user?.email ?? '',
      active: true,
    });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (product: Product) => {
    setEditing(product);
    setForm({
      name: product.name,
      description: product.description,
      characteristics: product.characteristics ?? '',
      category: product.category,
      price: String(product.price),
      availableQuantity: String(product.availableQuantity),
      unit: product.unit,
      contactPhone: product.contactPhone,
      contactEmail: product.contactEmail,
      active: product.active,
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload: ProductRequest = {
      name: form.name,
      description: form.description,
      characteristics: form.characteristics.trim() || undefined,
      category: form.category,
      price: Number(form.price),
      availableQuantity: Number(form.availableQuantity),
      unit: form.unit,
      contactPhone: form.contactPhone,
      contactEmail: form.contactEmail,
      active: form.active,
    };
    try {
      if (editing) {
        await productService.update(editing.id, payload);
        toast.success('Producto actualizado correctamente');
        setModalOpen(false);
        await load();
      } else {
        const created = await productService.create(payload);
        await load();
        toast.info('Producto publicado. Ahora puedes añadir fotografías reales.');
        openEdit(created);
      }
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (product: Product) => {
    try {
      await productService.changeStatus(product.id, !product.active);
      toast.success(product.active ? 'Producto desactivado' : 'Producto activado');
      await load();
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    }
  };

  const handleDelete = async () => {
    if (!toDelete) return;
    setSaving(true);
    try {
      await productService.remove(toDelete.id);
      toast.success('Producto eliminado correctamente');
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
        title="Mis productos"
        subtitle="Publica y administra los productos de tu catálogo."
        action={
          <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate}>
            Publicar producto
          </Button>
        }
      />

      {loading ? (
        <PageLoader />
      ) : products.length === 0 ? (
        <EmptyState
          icon={<Wheat className="h-8 w-8" />}
          title="Aún no has publicado productos"
          description="Publica tu primer producto para que los compradores puedan encontrarlo."
          action={
            <Button icon={<Plus className="h-4 w-4" />} onClick={openCreate}>
              Publicar producto
            </Button>
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {products.map((product) => {
            const status = getProductStatus(product);
            const unitLabel = MEASUREMENT_UNIT_OPTIONS.find((option) => option.value === product.unit)?.label ?? product.unit;
            return (
              <article key={product.id} className="card flex h-full flex-col overflow-hidden">
                {product.imageUrls?.[0] ? (
                  <img src={assetUrl(product.imageUrls[0])} alt={`Fotografía de ${product.name}`} className="h-36 w-full object-cover" />
                ) : null}
                <div className="flex flex-1 flex-col p-5">
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <span className="badge bg-agro-100 text-agro-800">{PRODUCT_CATEGORY_LABELS[product.category]}</span>
                    <Badge tone={PRODUCT_STATUS_META[status].tone}>{PRODUCT_STATUS_META[status].label}</Badge>
                  </div>
                  <h3 className="mt-3 text-lg font-semibold text-slate-900">{product.name}</h3>
                  <p className="mt-1 line-clamp-2 flex-1 text-sm text-slate-500">{product.description}</p>
                  {product.characteristics ? (
                    <p className="mt-2 line-clamp-2 text-xs text-slate-400">{product.characteristics}</p>
                  ) : null}
                  {status === 'inactive' ? <p className="mt-2 text-xs text-slate-500">No visible en el catálogo.</p> : null}
                  {status === 'soldOut' ? <p className="mt-2 text-xs text-amber-700">Sin existencias. Volverá al catálogo al registrar cantidad.</p> : null}
                  <dl className="mt-4 grid grid-cols-2 gap-3 rounded-xl bg-slate-50 p-3">
                    <div>
                      <dt className="text-xs text-slate-400">Precio</dt>
                      <dd className="mt-0.5 font-semibold text-agro-700">{formatCurrency(product.price)}</dd>
                    </div>
                    <div>
                      <dt className="text-xs text-slate-400">Cantidad disponible</dt>
                      <dd className="mt-0.5 font-semibold text-slate-800">
                        {formatNumber(product.availableQuantity)} {unitLabel}
                      </dd>
                    </div>
                  </dl>
                  <div className="mt-4 grid grid-cols-2 gap-2 border-t border-slate-100 pt-4">
                    <Button
                      variant="secondary"
                      className="justify-start px-3 py-1.5 text-xs"
                      icon={<Pencil className="h-4 w-4" />}
                      onClick={() => openEdit(product)}
                    >
                      Editar
                    </Button>
                    <Button
                      variant="secondary"
                      className="justify-start px-3 py-1.5 text-xs"
                      icon={<Camera className="h-4 w-4" />}
                      onClick={() => openEdit(product)}
                    >
                      Fotos
                    </Button>
                    <Button
                      variant="secondary"
                      className="justify-start px-3 py-1.5 text-xs"
                      icon={product.active ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      onClick={() => toggleStatus(product)}
                    >
                      {product.active ? 'Desactivar' : 'Activar'}
                    </Button>
                    <Button
                      variant="ghost"
                      className="justify-start px-3 py-1.5 text-xs text-red-600 hover:bg-red-50"
                      icon={<Trash2 className="h-4 w-4" />}
                      onClick={() => setToDelete(product)}
                    >
                      Eliminar
                    </Button>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}

      <Modal
        open={modalOpen}
        title={editing ? 'Editar producto' : 'Publicar producto'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setModalOpen(false)} disabled={saving}>
              Cancelar
            </Button>
            <Button type="submit" form="product-form" loading={saving}>
              {editing ? 'Guardar cambios' : 'Publicar'}
            </Button>
          </>
        }
      >
        <form id="product-form" className="space-y-4" onSubmit={handleSubmit} noValidate>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
          <TextField
            label="Nombre"
            name="name"
            required
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            placeholder="Tomate chonto fresco"
          />
          <TextAreaField
            label="Descripción"
            name="description"
            required
            value={form.description}
            onChange={(event) => setForm({ ...form, description: event.target.value })}
            placeholder="Describe el producto, su manejo y su calidad."
          />
          <TextAreaField
            label="Características o calidad"
            name="characteristics"
            value={form.characteristics}
            onChange={(event) => setForm({ ...form, characteristics: event.target.value })}
            placeholder="Grado de maduración, tamaño, certificaciones, manejo..."
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Categoría"
              name="category"
              required
              value={form.category}
              onChange={(event) => setForm({ ...form, category: event.target.value as ProductCategory })}
              options={PRODUCT_CATEGORY_OPTIONS}
            />
            <SelectField
              label="Unidad de medida"
              name="unit"
              required
              value={form.unit}
              onChange={(event) => setForm({ ...form, unit: event.target.value as MeasurementUnit })}
              options={MEASUREMENT_UNIT_OPTIONS}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField
              label="Precio (COP)"
              name="price"
              type="number"
              step="0.01"
              min="0.01"
              required
              value={form.price}
              onChange={(event) => setForm({ ...form, price: event.target.value })}
              placeholder="3500"
            />
            <TextField
              label="Cantidad disponible"
              name="availableQuantity"
              type="number"
              step="0.01"
              min="0"
              required
              value={form.availableQuantity}
              onChange={(event) => setForm({ ...form, availableQuantity: event.target.value })}
              placeholder="120"
            />
          </div>
          <p className="text-xs text-slate-500">
            Si la cantidad disponible es 0, el producto se marca como agotado automáticamente.
          </p>
          <div className="grid gap-4 sm:grid-cols-2">
            <TextField
              label="Teléfono de contacto"
              name="contactPhone"
              required
              value={form.contactPhone}
              onChange={(event) => setForm({ ...form, contactPhone: event.target.value })}
              placeholder="+57 300 000 0000"
            />
            <TextField
              label="Correo de contacto"
              name="contactEmail"
              type="email"
              required
              value={form.contactEmail}
              onChange={(event) => setForm({ ...form, contactEmail: event.target.value })}
              placeholder="tucorreo@agroconecta.co"
            />
          </div>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(event) => setForm({ ...form, active: event.target.checked })}
              className="h-4 w-4 rounded border-slate-300 text-agro-600"
            />
            Publicar como activo (visible en el catálogo)
          </label>
        </form>
        {editing ? (
          <div className="mt-5">
            <ProductImageManager
              productId={editing.id}
              onChanged={() => {
                void load();
              }}
            />
          </div>
        ) : null}
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Eliminar producto"
        message={`¿Seguro que deseas eliminar "${toDelete?.name}"?`}
        loading={saving}
        onConfirm={handleDelete}
        onCancel={() => setToDelete(null)}
      />
    </>
  );
}
