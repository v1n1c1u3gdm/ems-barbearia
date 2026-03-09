import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

import { AdminLayout } from './AdminLayout';

describe('AdminLayout', () => {
  it('renders admin header and outlet', () => {
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<span>Dashboard</span>} />
          </Route>
          <Route path="/admin/login" element={<span>Login</span>} />
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: /EMS Barbearia - Admin/i })).toHaveAttribute('href', '/admin');
    expect(screen.getByRole('button', { name: 'Sair' })).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });

  it('navigates to login when Sair is clicked', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<span>Dashboard</span>} />
          </Route>
          <Route path="/admin/login" element={<span>Login</span>} />
        </Routes>
      </MemoryRouter>
    );
    await user.click(screen.getByRole('button', { name: 'Sair' }));
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  it('renders Agendamentos nav as active when on agendamentos route', () => {
    render(
      <MemoryRouter initialEntries={['/admin/agendamentos']}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<span>Dashboard</span>} />
            <Route path="agendamentos" element={<span>Agendamentos</span>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );
    const agendamentosLink = screen.getByRole('link', { name: 'Agendamentos' });
    expect(agendamentosLink).toHaveClass('bg-zinc-800');
  });

  it('renders public nav links on login page', () => {
    render(
      <MemoryRouter initialEntries={['/admin/login']}>
        <Routes>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<span>Dashboard</span>} />
          </Route>
          <Route path="/admin/login" element={<AdminLayout />}>
            <Route index element={<span>Login</span>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: 'Início' })).toHaveAttribute('href', '/#inicio');
    expect(screen.getByRole('link', { name: 'Serviços' })).toHaveAttribute('href', '/#servicos');
    expect(screen.getByRole('link', { name: 'Contato' })).toHaveAttribute('href', '/#contato');
    expect(screen.getByRole('link', { name: /EMS Barbearia - Início/i })).toHaveAttribute('href', '/');
  });
});
