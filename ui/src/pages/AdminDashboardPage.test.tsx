import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';
import { AdminDashboardPage } from './AdminDashboardPage';

vi.mock('@/features/admin/api', () => ({
  fetchDashboardSummary: vi.fn(() =>
    Promise.resolve({
      relacionamentos: 10,
      clientes: 5,
      agendamentos: 3,
      servicos: 2,
      ultimaAtualizacao: '2025-03-07T12:00:00Z',
    })
  ),
}));

function wrap(ui: React.ReactNode) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        {ui}
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe('AdminDashboardPage', () => {
  it('renders Painel heading', () => {
    wrap(<AdminDashboardPage />);
    expect(screen.getByRole('heading', { name: 'Painel' })).toBeInTheDocument();
  });

  it('renders links to admin areas with counts when summary loads', async () => {
    wrap(<AdminDashboardPage />);
    await screen.findByText('(10)');
    expect(screen.getByRole('link', { name: /Relacionamentos.*10/ })).toHaveAttribute('href', '/admin/relacionamentos');
    expect(screen.getByRole('link', { name: /Serviços.*2/ })).toHaveAttribute('href', '/admin/servicos');
    expect(screen.getByRole('link', { name: /Agendamentos.*3/ })).toHaveAttribute('href', '/admin/agendamentos');
    expect(screen.getByRole('link', { name: /Clientes.*5/ })).toHaveAttribute('href', '/admin/clientes');
  });

  it('shows ultima atualização when summary has ultimaAtualizacao', async () => {
    wrap(<AdminDashboardPage />);
    await screen.findByText(/Última atualização/);
    expect(screen.getByText(/Última atualização \(qualquer área\)/)).toBeInTheDocument();
  });
});
