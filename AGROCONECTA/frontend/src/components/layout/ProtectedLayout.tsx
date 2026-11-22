import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { AppLayout } from './AppLayout';
import { PageLoader } from '../ui/Feedback';

export function ProtectedLayout() {
  const { isAuthenticated, initializing } = useAuth();

  if (initializing) {
    return <PageLoader />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/iniciar-sesion" replace />;
  }

  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
}
