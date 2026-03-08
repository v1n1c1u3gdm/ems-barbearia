import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { PageLayout } from '@/components/layout/PageLayout';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { ProtectedAdminRoute } from '@/components/admin/ProtectedAdminRoute';
import { LandingPage } from '@/pages/LandingPage';
import { AdminLoginPage } from '@/pages/AdminLoginPage';
import { AdminDashboardPage } from '@/pages/AdminDashboardPage';
import { AdminContatosPage } from '@/pages/admin/AdminContatosPage';
import { AdminPromocoesPage } from '@/pages/admin/AdminPromocoesPage';
import { AdminAgendamentosPage } from '@/pages/admin/AdminAgendamentosPage';
import { AdminClientesPage } from '@/pages/admin/AdminClientesPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <PageLayout />,
    children: [
      { index: true, element: <LandingPage /> },
    ],
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { path: 'login', element: <AdminLoginPage /> },
      {
        element: <ProtectedAdminRoute />,
        children: [
          { index: true, element: <AdminDashboardPage /> },
          { path: 'contatos', element: <AdminContatosPage /> },
          { path: 'promocoes', element: <AdminPromocoesPage /> },
          { path: 'agendamentos', element: <AdminAgendamentosPage /> },
          { path: 'clientes', element: <AdminClientesPage /> },
        ],
      },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
