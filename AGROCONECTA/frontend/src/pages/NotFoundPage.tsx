import { Link } from 'react-router-dom';
import { Sprout } from 'lucide-react';

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-agro-50 px-4 text-center">
      <span className="flex h-16 w-16 items-center justify-center rounded-2xl bg-agro-100 text-agro-700">
        <Sprout className="h-8 w-8" />
      </span>
      <h1 className="text-3xl font-bold text-slate-900">Página no encontrada</h1>
      <p className="max-w-md text-slate-500">
        La dirección que buscas no existe o fue movida. Vuelve al inicio para continuar.
      </p>
      <Link to="/" className="btn-primary">
        Volver al inicio
      </Link>
    </div>
  );
}
