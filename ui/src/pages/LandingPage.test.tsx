import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';

import { LandingPage } from './LandingPage';

vi.mock('@/features/admin/api', () => ({
  fetchPublicServicos: vi.fn(() =>
    Promise.resolve([
      { id: 1, titulo: 'Corte', descricao: 'Corte masculino.', ativo: true, duracaoMinutos: 45, createdAt: '', validoDe: null, validoAte: null },
      { id: 2, titulo: 'Barba', descricao: null, ativo: true, duracaoMinutos: 30, createdAt: '', validoDe: null, validoAte: null },
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

  it('renders default description when servico has no descricao', async () => {
    wrap(<LandingPage />);
    await screen.findByText('Serviço com qualidade EMS Barbearia.');
  });

  it('shows loading message while services load', async () => {
    const { fetchPublicServicos } = await import('@/features/admin/api');
    vi.mocked(fetchPublicServicos).mockImplementationOnce(() => new Promise(() => {}));
    const loadingClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    render(
      <MemoryRouter>
        <QueryClientProvider client={loadingClient}>
          <LandingPage />
        </QueryClientProvider>
      </MemoryRouter>
    );
    expect(screen.getByText(/Carregando serviços/)).toBeInTheDocument();
  });

  it('shows error message when services fail to load', async () => {
    const { fetchPublicServicos } = await import('@/features/admin/api');
    vi.mocked(fetchPublicServicos).mockRejectedValueOnce(new Error('Network error'));
    const errorClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    render(
      <MemoryRouter>
        <QueryClientProvider client={errorClient}>
          <LandingPage />
        </QueryClientProvider>
      </MemoryRouter>
    );
    await screen.findByText(/Não foi possível carregar os serviços/);
  });

  it('scrolls to section when hash is present', async () => {
    const scrollIntoView = vi.fn();
    const el = document.createElement('div');
    el.id = 'servicos';
    el.scrollIntoView = scrollIntoView;
    document.body.appendChild(el);
    render(
      <MemoryRouter initialEntries={['/#servicos']}>
        <QueryClientProvider client={queryClient}>
          <LandingPage />
        </QueryClientProvider>
      </MemoryRouter>
    );
    await waitFor(() => {
      expect(scrollIntoView).toHaveBeenCalled();
    });
    document.body.removeChild(el);
  });
});
