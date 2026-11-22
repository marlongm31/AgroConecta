import { Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { ProtectedLayout } from './components/layout/ProtectedLayout';
import { ProtectedRoute } from './components/layout/ProtectedRoute';
import { PageLoader } from './components/ui/Feedback';
import { useAuth } from './context/AuthContext';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ParcelsPage } from './pages/ParcelsPage';
import { CropsPage } from './pages/CropsPage';
import { ActivitiesPage } from './pages/ActivitiesPage';
import { ExpensesPage } from './pages/ExpensesPage';
import { ProductionPage } from './pages/ProductionPage';
import { MyProductsPage } from './pages/MyProductsPage';
import { CatalogPage } from './pages/CatalogPage';
import { PublicCatalogPage } from './pages/PublicCatalogPage';
import { ProfilePage } from './pages/ProfilePage';
import { UsersPage } from './pages/UsersPage';
import { SoilQuestionnairePage } from './pages/SoilQuestionnairePage';
import { WeatherPage } from './pages/WeatherPage';
import { ChatPage } from './pages/ChatPage';
import { NotFoundPage } from './pages/NotFoundPage';

function HomeRoute() {
  const { isAuthenticated, initializing } = useAuth();
  if (initializing) return <PageLoader />;
  if (!isAuthenticated) return <LandingPage />;
  return (
    <AppLayout>
      <DashboardPage />
    </AppLayout>
  );
}

function CatalogRoute() {
  const { isAuthenticated, initializing } = useAuth();
  if (initializing) return <PageLoader />;
  if (!isAuthenticated) return <PublicCatalogPage />;
  return (
    <AppLayout>
      <CatalogPage />
    </AppLayout>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRoute />} />
      <Route path="/iniciar-sesion" element={<LoginPage />} />
      <Route path="/registro" element={<RegisterPage />} />
      <Route path="/catalogo" element={<CatalogRoute />} />

      <Route element={<ProtectedLayout />}>
        <Route
          path="/parcelas"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <ParcelsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/cultivos"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <CropsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/actividades"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <ActivitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/gastos"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <ExpensesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/produccion"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <ProductionPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mis-productos"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <MyProductsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/suelo"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <SoilQuestionnairePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clima"
          element={
            <ProtectedRoute roles={['PRODUCTOR']}>
              <WeatherPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/usuarios"
          element={
            <ProtectedRoute roles={['ADMINISTRADOR']}>
              <UsersPage />
            </ProtectedRoute>
          }
        />
        <Route path="/mensajes" element={<ChatPage />} />
        <Route path="/perfil" element={<ProfilePage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
