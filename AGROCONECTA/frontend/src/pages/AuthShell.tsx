import { Leaf } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

export function AuthShell({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer: ReactNode;
}) {
  return (
    <div className="flex min-h-screen">
      <div className="hidden w-1/2 flex-col justify-between bg-agro-700 p-10 text-white lg:flex">
        <Link to="/" className="flex items-center gap-2">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15">
            <Leaf className="h-6 w-6" />
          </span>
          <span className="text-lg font-bold">AgroConecta</span>
        </Link>
        <div>
          <h2 className="text-3xl font-bold leading-tight">
            La plataforma de los productores del Caribe colombiano
          </h2>
          <p className="mt-4 max-w-md text-agro-100">
            Administra tus parcelas, cultivos, actividades y gastos, y publica tu producción en un
            catálogo digital.
          </p>
        </div>
        <p className="text-sm text-agro-200">Proyecto académico · Arquitectura Hexagonal</p>
      </div>
      <div className="flex w-full items-center justify-center px-4 py-10 lg:w-1/2">
        <div className="w-full max-w-md">
          <Link to="/" className="mb-8 flex items-center gap-2 lg:hidden">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-agro-600 text-white">
              <Leaf className="h-6 w-6" />
            </span>
            <span className="text-lg font-bold text-slate-900">AgroConecta</span>
          </Link>
          <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
          <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
          <div className="mt-6">{children}</div>
          <div className="mt-6 text-sm text-slate-500">{footer}</div>
        </div>
      </div>
    </div>
  );
}
