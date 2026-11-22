import {
  ArrowRight,
  BarChart3,
  Coins,
  Leaf,
  ShieldCheck,
  Sprout,
  Store,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const FEATURES = [
  {
    icon: <Sprout className="h-6 w-6" />,
    title: 'Gestión de cultivos',
    description: 'Registra parcelas, cultivos y actividades agrícolas en un solo lugar.',
  },
  {
    icon: <Coins className="h-6 w-6" />,
    title: 'Control de gastos',
    description: 'Lleva el control de los costos de producción con montos precisos.',
  },
  {
    icon: <BarChart3 className="h-6 w-6" />,
    title: 'Producción real',
    description: 'Consulta indicadores reales calculados a partir de tu información.',
  },
  {
    icon: <Store className="h-6 w-6" />,
    title: 'Catálogo digital',
    description: 'Publica tus productos y conecta con compradores de la región Caribe.',
  },
];

export function LandingPage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen bg-white">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-4 py-5 sm:px-6">
        <div className="flex items-center gap-2">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-agro-600 text-white">
            <Leaf className="h-6 w-6" />
          </span>
          <span className="text-lg font-bold text-slate-900">AgroConecta</span>
        </div>
        <nav className="flex items-center gap-3">
          {isAuthenticated ? (
            <Link to="/" className="btn-primary">
              Ir al panel
            </Link>
          ) : (
            <>
              <Link to="/iniciar-sesion" className="btn-secondary">
                Iniciar sesión
              </Link>
              <Link to="/registro" className="btn-primary">
                Registrarse
              </Link>
            </>
          )}
        </nav>
      </header>

      <section className="mx-auto grid max-w-6xl items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-2 lg:py-20">
        <div>
          <span className="badge bg-agro-100 text-agro-800">Región Caribe colombiana</span>
          <h1 className="mt-4 text-4xl font-extrabold leading-tight text-slate-900 sm:text-5xl">
            Del campo a tu mesa, con información clara y confiable
          </h1>
          <p className="mt-5 max-w-xl text-lg text-slate-600">
            AgroConecta centraliza la gestión de parcelas, cultivos, actividades, gastos y producción
            de los pequeños productores agrícolas, y conecta sus productos con nuevos compradores.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/registro" className="btn-primary">
              Comenzar gratis
              <ArrowRight className="h-4 w-4" />
            </Link>
            <Link to="/catalogo" className="btn-secondary">
              Explorar catálogo
            </Link>
          </div>
          <p className="mt-6 flex items-center gap-2 text-sm text-slate-500">
            <ShieldCheck className="h-4 w-4 text-agro-600" />
            Información protegida y aislada por productor.
          </p>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="card p-5">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-agro-100 text-agro-700">
                {feature.icon}
              </span>
              <h3 className="mt-4 text-base font-semibold text-slate-900">{feature.title}</h3>
              <p className="mt-1 text-sm text-slate-500">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      <footer className="border-t border-slate-100 py-8">
        <p className="text-center text-sm text-slate-400">
          AgroConecta · Proyecto académico de Arquitectura Hexagonal · {new Date().getFullYear()}
        </p>
      </footer>
    </div>
  );
}
