import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';

import { LandingPage } from './LandingPage';

vi.mock('@/features/admin/api', () => ({
  fetchPublicServicos: vi.fn(() =>
    Promise.resolve([
      { id: 1, titulo: 'Corte', descricao: 'Corte masculino.', ativo: true, duracaoMinutos: 45, createdAt: '', validoDe: null, validoAte: null },
      { id: 2, titulo: 'Barba', descricao: 'Barba completa.', ativo: true, duracaoMinutos: 30, createdAt: '', validoDe: null, validoAte: null },
    ])
  ),
}));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

function wrap(ui: React.ReactNode) {
  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        {ui}
      </QueryClientProvider>
    </MemoryRouter>
  );
}

describe('LandingPage', () => {
  it('renders main heading', () => {
    wrap(<LandingPage />);
    expect(
      screen.getByRole('heading', { name: /barbearia para quem dita o próprio ritmo/i })
    ).toBeInTheDocument();
  });

  it('renders Nossos serviços section', () => {
    wrap(<LandingPage />);
    expect(screen.getByRole('heading', { name: 'Nossos serviços' })).toBeInTheDocument();
  });

  it('renders Agende seu horário section', () => {
    wrap(<LandingPage />);
    expect(screen.getByRole('heading', { name: 'Agende seu horário' })).toBeInTheDocument();
  });
});
