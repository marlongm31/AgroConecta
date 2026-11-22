import {
  BarChart3,
  Coins,
  Leaf,
  Package,
  Sprout,
  Store,
  TrendingUp,
  Users,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardService } from '../services/dashboardService';
import { userService } from '../services/userService';
import { StatCard, PageHeader } from '../components/ui/Display';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { ProductCard } from '../components/catalog/ProductCard';
import { ProductDetailModal } from '../components/catalog/ProductDetailModal';
import { useProductContact } from '../components/catalog/useProductContact';
import { ACTIVITY_TYPE_LABELS } from '../utils/labels';
import { formatCurrency, formatDate, formatNumber } from '../utils/format';
import { getErrorMessage } from '../lib/errors';
import type { BuyerDashboard, ProducerDashboard, Product, User } from '../types';

function ProducerPanel() {
  const [data, setData] = useState<ProducerDashboard | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService
      .producer()
      .then(setData)
      .catch((caught) => setError(getErrorMessage(caught)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageLoader />;
  if (error || !data) {
    return <EmptyState icon={<Leaf className="h-8 w-8" />} title="No fue posible cargar el panel" description={error} />;
  }

  return (
    <>
      <PageHeader title="Panel del productor" subtitle="Resumen real de tu operación agrícola." />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <StatCard label="Parcelas" value={formatNumber(data.parcelCount)} icon={<Leaf className="h-5 w-5" />} />
        <StatCard label="Cultivos activos" value={formatNumber(data.activeCropCount)} icon={<Sprout className="h-5 w-5" />} tone="blue" />
        <StatCard label="Gastos totales" value={formatCurrency(data.totalExpenses)} icon={<Coins className="h-5 w-5" />} tone="amber" />
        <StatCard label="Registros de producción" value={formatNumber(data.productionRecordCount)} icon={<BarChart3 className="h-5 w-5" />} tone="slate" />
        <StatCard label="Productos publicados" value={formatNumber(data.publishedProductCount)} icon={<Store className="h-5 w-5" />} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <section className="card p-5 lg:col-span-2">
          <h2 className="mb-4 text-base font-semibold text-slate-900">Actividades recientes</h2>
          {data.recentActivities.length === 0 ? (
            <p className="text-sm text-slate-500">Aún no has registrado actividades agrícolas.</p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {data.recentActivities.map((activity) => (
                <li key={activity.id} className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-medium text-slate-800">{activity.description}</p>
                    <p className="text-xs text-slate-400">
                      {ACTIVITY_TYPE_LABELS[activity.type]} · {formatDate(activity.activityDate)}
                    </p>
                  </div>
                  <span className="badge bg-agro-100 text-agro-800">{ACTIVITY_TYPE_LABELS[activity.type]}</span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card p-5">
          <h2 className="mb-4 text-base font-semibold text-slate-900">Accesos rápidos</h2>
          <div className="flex flex-col gap-2">
            <Link to="/parcelas" className="btn-secondary justify-start">
              <Leaf className="h-4 w-4" /> Gestionar parcelas
            </Link>
            <Link to="/cultivos" className="btn-secondary justify-start">
              <Sprout className="h-4 w-4" /> Gestionar cultivos
            </Link>
            <Link to="/gastos" className="btn-secondary justify-start">
              <Coins className="h-4 w-4" /> Registrar gasto
            </Link>
            <Link to="/mis-productos" className="btn-primary justify-start">
              <Store className="h-4 w-4" /> Publicar producto
            </Link>
          </div>
        </section>
      </div>
    </>
  );
}

function BuyerPanel() {
  const { contact, startingProductId, canContactProduct, contactLabel } = useProductContact();
  const [data, setData] = useState<BuyerDashboard | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService
      .buyer()
      .then(setData)
      .catch((caught) => setError(getErrorMessage(caught)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageLoader />;
  if (error || !data) {
    return <EmptyState icon={<Store className="h-8 w-8" />} title="No fue posible cargar el panel" description={error} />;
  }

  const selectedCanContact = selectedProduct ? canContactProduct(selectedProduct) : false;

  return (
    <>
      <PageHeader title="Panel del comprador" subtitle="Descubre productos frescos de la región Caribe." />
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Productos disponibles" value={formatNumber(data.availableProductCount)} icon={<Store className="h-5 w-5" />} />
        <StatCard label="Novedades" value={formatNumber(data.recentProducts.length)} icon={<TrendingUp className="h-5 w-5" />} tone="blue" />
        <StatCard label="Categorías" value="6" icon={<Package className="h-5 w-5" />} tone="amber" hint="Frutas, hortalizas y más" />
      </div>

      <section className="mt-6">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-slate-900">Productos recientes</h2>
          <Link to="/catalogo" className="text-sm font-semibold text-agro-700 hover:underline">
            Ver catálogo completo
          </Link>
        </div>
        {data.recentProducts.length === 0 ? (
          <EmptyState icon={<Store className="h-8 w-8" />} title="Todavía no hay productos publicados" />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.recentProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onViewDetails={setSelectedProduct}
                onContact={canContactProduct(product) ? contact : undefined}
                contactLabel={contactLabel}
                contactLoading={startingProductId === product.id}
              />
            ))}
          </div>
        )}
      </section>

      <ProductDetailModal
        product={selectedProduct}
        open={Boolean(selectedProduct)}
        onClose={() => setSelectedProduct(null)}
        onContact={selectedCanContact ? contact : undefined}
        contactLabel={contactLabel}
        contactLoading={Boolean(selectedProduct && startingProductId === selectedProduct.id)}
      />
    </>
  );
}

function AdminPanel() {
  const [users, setUsers] = useState<User[]>([]);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userService
      .list(0, 5)
      .then((page) => {
        setUsers(page.content);
        setTotal(page.totalElements);
      })
      .catch((caught) => setError(getErrorMessage(caught)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageLoader />;

  return (
    <>
      <PageHeader title="Panel de administración" subtitle="Gestiona los usuarios de la plataforma." />
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Usuarios registrados" value={formatNumber(total)} icon={<Users className="h-5 w-5" />} />
        <StatCard label="Productores" value={formatNumber(users.filter((user) => user.role === 'PRODUCTOR').length)} icon={<Sprout className="h-5 w-5" />} tone="blue" hint="En la primera página" />
        <StatCard label="Compradores" value={formatNumber(users.filter((user) => user.role === 'COMPRADOR').length)} icon={<Store className="h-5 w-5" />} tone="amber" hint="En la primera página" />
      </div>
      {error ? <p className="mt-4 text-sm text-red-600">{error}</p> : null}
      <div className="mt-6 card overflow-hidden">
        <table className="table-base">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Rol</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td className="font-medium text-slate-800">{user.fullName}</td>
                <td>{user.email}</td>
                <td>{user.role}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="p-4">
          <Link to="/usuarios" className="text-sm font-semibold text-agro-700 hover:underline">
            Administrar usuarios
          </Link>
        </div>
      </div>
    </>
  );
}

export function DashboardPage() {
  const { user } = useAuth();
  if (user?.role === 'COMPRADOR') return <BuyerPanel />;
  if (user?.role === 'ADMINISTRADOR') return <AdminPanel />;
  return <ProducerPanel />;
}
