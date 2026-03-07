import { render, screen } from '@testing-library/react';
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
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: /EMS Barbearia — Admin/i })).toHaveAttribute('href', '/admin');
    expect(screen.getByRole('link', { name: 'Voltar ao site' })).toHaveAttribute('href', '/');
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });
});
