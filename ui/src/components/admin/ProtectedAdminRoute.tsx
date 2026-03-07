import { Navigate, Outlet } from 'react-router-dom';
import { isAuthenticated } from '@/config/auth';

export function ProtectedAdminRoute() {
  if (!isAuthenticated()) {
    return <Navigate to="/admin/login" replace />;
  }
  return <Outlet />;
}
