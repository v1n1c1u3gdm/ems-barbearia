import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { vi } from 'vitest';

import { isAuthenticated } from '@/config/auth';

import { ProtectedAdminRoute } from './ProtectedAdminRoute';

vi.mock('@/config/auth', () => ({
  isAuthenticated: vi.fn(),
}));

describe('ProtectedAdminRoute', () => {
  it('redirects to admin login when not authenticated', () => {
    vi.mocked(isAuthenticated).mockReturnValue(false);

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/admin" element={<ProtectedAdminRoute />}>
            <Route index element={<span>Dashboard</span>} />
          </Route>
          <Route path="/admin/login" element={<span>Login page</span>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login page')).toBeInTheDocument();
    expect(screen.queryByText('Dashboard')).not.toBeInTheDocument();
  });

  it('renders outlet when authenticated', () => {
    vi.mocked(isAuthenticated).mockReturnValue(true);

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/admin" element={<ProtectedAdminRoute />}>
            <Route index element={<span>Dashboard</span>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });
});
