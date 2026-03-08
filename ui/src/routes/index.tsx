import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { PageLayout } from '@/components/layout/PageLayout';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { ProtectedAdminRoute } from '@/components/admin/ProtectedAdminRoute';
import { LandingPage } from '@/pages/LandingPage';
import { AdminLoginPage } from '@/pages/AdminLoginPage';
import { AdminDashboardPage } from '@/pages/AdminDashboardPage';
import { AdminRelacionamentosPage } from '@/pages/admin/AdminRelacionamentosPage';
import { AdminServicosPage } from '@/pages/admin/AdminServicosPage';
import { AdminStaffPage } from '@/pages/admin/AdminStaffPage';
import { AdminAgendamentosPage } from '@/pages/admin/AdminAgendamentosPage';
import { AdminAssinaturasPage } from '@/pages/admin/AdminAssinaturasPage';
import { AdminClientesPage } from '@/pages/admin/AdminClientesPage';
import { AgendarPage } from '@/pages/AgendarPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <PageLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'agendar', element: <AgendarPage /> },
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
          { path: 'relacionamentos', element: <AdminRelacionamentosPage /> },
          { path: 'servicos', element: <AdminServicosPage /> },
          { path: 'staff', element: <AdminStaffPage /> },
          { path: 'agendamentos', element: <AdminAgendamentosPage /> },
          { path: 'assinaturas', element: <AdminAssinaturasPage /> },
          { path: 'clientes', element: <AdminClientesPage /> },
        ],
      },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
