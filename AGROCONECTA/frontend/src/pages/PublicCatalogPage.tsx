import { Leaf } from 'lucide-react';
import { Link } from 'react-router-dom';
import { CatalogView } from '../components/catalog/CatalogView';

export function PublicCatalogPage() {
  return (
    <div className="min-h-screen bg-agro-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <Link to="/" className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-agro-600 text-white">
              <Leaf className="h-5 w-5" />
            </span>
            <span className="text-lg font-bold text-slate-900">AgroConecta</span>
          </Link>
          <nav className="flex items-center gap-3">
            <Link to="/iniciar-sesion" className="btn-secondary">
              Iniciar sesión
            </Link>
            <Link to="/registro" className="btn-primary">
              Registrarse
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-slate-900">Catálogo de productos</h1>
          <p className="mt-1 text-sm text-slate-500">
            Productos frescos publicados por los productores de la región Caribe.
          </p>
        </div>
        <CatalogView />
      </main>
    </div>
  );
}
