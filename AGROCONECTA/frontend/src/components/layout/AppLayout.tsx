import {
  BarChart3,
  Bug,
  CloudSun,
  Coins,
  LayoutDashboard,
  MessageCircle,
  Mountain,
  Leaf,
  LogOut,
  Menu,
  Package,
  Sprout,
  Store,
  UserCircle,
  Users,
  Wheat,
  X,
} from 'lucide-react';
import { useEffect, useState, type ReactNode } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useUnreadMessages } from '../../context/useUnreadMessages';
import { canUseChat, ROLE_LABELS } from '../../utils/labels';
import type { Role } from '../../types';

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  roles?: Role[];
}

const NAV_ITEMS: NavItem[] = [
  { to: '/', label: 'Panel', icon: <LayoutDashboard className="h-5 w-5" /> },
  { to: '/parcelas', label: 'Parcelas', icon: <Leaf className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/cultivos', label: 'Cultivos', icon: <Sprout className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/actividades', label: 'Actividades', icon: <Bug className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/gastos', label: 'Gastos', icon: <Coins className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/produccion', label: 'Producción', icon: <BarChart3 className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/mis-productos', label: 'Mis productos', icon: <Wheat className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/suelo', label: 'Suelo', icon: <Mountain className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/clima', label: 'Clima', icon: <CloudSun className="h-5 w-5" />, roles: ['PRODUCTOR'] },
  { to: '/catalogo', label: 'Catálogo', icon: <Store className="h-5 w-5" /> },
  { to: '/mensajes', label: 'Mensajes', icon: <MessageCircle className="h-5 w-5" /> },
  { to: '/usuarios', label: 'Usuarios', icon: <Users className="h-5 w-5" />, roles: ['ADMINISTRADOR'] },
  { to: '/perfil', label: 'Mi perfil', icon: <UserCircle className="h-5 w-5" /> },
];

export function AppLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const { count: unreadCount } = useUnreadMessages();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);
  const isChatRoute = location.pathname === '/mensajes';

  useEffect(() => {
    if (!isChatRoute) return;
    const updateViewport = () => {
      const height = window.visualViewport?.height ?? window.innerHeight;
      document.documentElement.style.setProperty('--chat-viewport-height', `${height}px`);
    };
    document.body.classList.add('chat-route');
    updateViewport();
    window.visualViewport?.addEventListener('resize', updateViewport);
    window.addEventListener('resize', updateViewport);
    return () => {
      document.body.classList.remove('chat-route');
      document.documentElement.style.removeProperty('--chat-viewport-height');
      window.visualViewport?.removeEventListener('resize', updateViewport);
      window.removeEventListener('resize', updateViewport);
    };
  }, [isChatRoute]);

  const items = NAV_ITEMS.filter((item) => !item.roles || (user && item.roles.includes(user.role)));
  const showUnreadBadge = canUseChat(user?.role) && unreadCount > 0;

  const handleLogout = async () => {
    await logout();
    navigate('/iniciar-sesion', { replace: true });
  };

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
      isActive ? 'bg-agro-600 text-white shadow-sm' : 'text-slate-600 hover:bg-agro-50 hover:text-agro-700'
    }`;

  return (
    <div className={isChatRoute ? 'chat-layout-root bg-agro-50' : 'min-h-screen bg-agro-50'}>
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 transform border-r border-slate-200 bg-white transition-transform lg:translate-x-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-16 items-center gap-2 border-b border-slate-100 px-5">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-agro-600 text-white">
            <Leaf className="h-5 w-5" />
          </span>
          <div>
            <p className="text-sm font-bold text-slate-900">AgroConecta</p>
            <p className="text-xs text-slate-400">Gestión agrícola</p>
          </div>
        </div>
        <nav className="flex flex-col gap-1 p-3">
          {items.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'} className={linkClass} onClick={() => setOpen(false)}>
              {item.icon}
              <span className="flex-1">{item.label}</span>
              {item.to === '/mensajes' && showUnreadBadge ? (
                <span
                  aria-label={`${unreadCount} mensajes sin leer`}
                  className="min-w-5 rounded-full bg-amber-400 px-1.5 py-0.5 text-center text-xs font-bold text-slate-900"
                >
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              ) : null}
            </NavLink>
          ))}
        </nav>
        <div className="absolute inset-x-0 bottom-0 border-t border-slate-100 p-3">
          <button type="button" onClick={handleLogout} className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50">
            <LogOut className="h-5 w-5" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      {open ? (
        <div className="fixed inset-0 z-20 bg-slate-900/40 lg:hidden" onClick={() => setOpen(false)} aria-hidden="true" />
      ) : null}

      <div className={`lg:pl-64 ${isChatRoute ? 'chat-layout-column' : ''}`}>
        <header className={`z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white/90 px-4 backdrop-blur sm:px-6 ${
          isChatRoute ? 'chat-layout-header shrink-0' : 'sticky top-0'
        }`}>
          <button
            type="button"
            aria-label="Abrir menú"
            className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
            onClick={() => setOpen((value) => !value)}
          >
            {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
          <div className="hidden lg:block">
            <p className="text-sm font-semibold text-slate-800">
              Hola, {user?.fullName.split(' ')[0] ?? 'usuario'}
            </p>
            <p className="text-xs text-slate-400">{user ? ROLE_LABELS[user.role] : ''}</p>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-slate-500 sm:block">{user?.email}</span>
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-agro-100 text-sm font-bold text-agro-700">
              {user?.fullName.charAt(0).toUpperCase() ?? <Package className="h-4 w-4" />}
            </span>
          </div>
        </header>
        <main className={isChatRoute ? 'chat-layout-main' : 'mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:py-8'}>
          {children}
        </main>
      </div>
    </div>
  );
}
