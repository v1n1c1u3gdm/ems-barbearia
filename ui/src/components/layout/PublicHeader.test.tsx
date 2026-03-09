import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';

import { getPublicToken } from '@/features/public/auth';
import { PublicAuthProvider } from '@/features/public/PublicAuthContext';

import { PublicHeader } from './PublicHeader';

vi.mock('@/features/public/auth', () => ({
  getPublicToken: vi.fn(() => null),
  setPublicToken: vi.fn(),
  clearPublicToken: vi.fn(),
}));

vi.mock('@/features/public/api', () => ({
  fetchMyAgendamentos: vi.fn(() => Promise.resolve([])),
  cancelPublicAgendamento: vi.fn(() => Promise.resolve(null)),
}));

function wrap(ui: React.ReactElement) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>{ui}</MemoryRouter>
    </QueryClientProvider>
  );
}

function wrapWithAuth(ui: React.ReactElement) {
  vi.mocked(getPublicToken).mockReturnValueOnce('token');
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <PublicAuthProvider>
        <MemoryRouter>{ui}</MemoryRouter>
      </PublicAuthProvider>
    </QueryClientProvider>
  );
}

describe('PublicHeader', () => {
  it('renders logo link to home', () => {
    wrap(<PublicHeader />);
    const logo = screen.getByRole('link', { name: /EMS Barbearia - Início/i });
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveAttribute('href', '/');
  });

  it('renders nav links Início, Serviços, Contato', () => {
    wrap(<PublicHeader />);
    expect(screen.getByRole('link', { name: 'Início' })).toHaveAttribute('href', '/#inicio');
    expect(screen.getByRole('link', { name: 'Serviços' })).toHaveAttribute('href', '/#servicos');
    expect(screen.getByRole('link', { name: 'Contato' })).toHaveAttribute('href', '/#contato');
  });

  it('when logged in shows notifications button and logout in dropdown', async () => {
    const user = userEvent.setup();
    wrapWithAuth(<PublicHeader />);
    expect(screen.getByRole('button', { name: /Notificações/i })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Notificações/i }));
    await screen.findByText('Meus agendamentos');
    expect(screen.getByRole('button', { name: /Sair/i })).toBeInTheDocument();
  });

  it('when logged in with agendamentos shows Confirmado in dropdown (cancel is on Agendar page)', async () => {
    const { fetchMyAgendamentos } = await import('@/features/public/api');
    vi.mocked(fetchMyAgendamentos).mockResolvedValueOnce([
      {
        id: 1,
        clienteId: 10,
        clienteNome: 'Eu',
        servicoId: 1,
        servicoTitulo: 'Corte',
        staffId: 1,
        staffNome: 'João',
        dataHora: '2026-03-10T14:00:00.000Z',
        dataHoraFim: null,
        tipo: 'FIRME',
        status: 'APROVADO',
        createdAt: '2026-03-09T00:00:00.000Z',
      },
    ]);
    const user = userEvent.setup();
    wrapWithAuth(<PublicHeader />);
    await user.click(screen.getByRole('button', { name: /Notificações/i }));
    await screen.findByText('Confirmado');
    expect(screen.queryByRole('button', { name: 'Cancelar' })).not.toBeInTheDocument();
  });

  it('when logged in shows Pendente and Cancelado labels for agendamentos', async () => {
    const { fetchMyAgendamentos } = await import('@/features/public/api');
    vi.mocked(fetchMyAgendamentos).mockResolvedValueOnce([
      {
        id: 1,
        clienteId: 10,
        clienteNome: 'Eu',
        servicoId: 1,
        servicoTitulo: 'Corte',
        staffId: 1,
        staffNome: 'João',
        dataHora: '2026-03-10T14:00:00.000Z',
        dataHoraFim: null,
        tipo: 'FIRME',
        status: 'PENDENTE',
        createdAt: '2026-03-09T00:00:00.000Z',
      },
      {
        id: 2,
        clienteId: 10,
        clienteNome: 'Eu',
        servicoId: 1,
        servicoTitulo: 'Barba',
        staffId: 1,
        staffNome: 'João',
        dataHora: '2026-03-11T10:00:00.000Z',
        dataHoraFim: null,
        tipo: 'ENCAIXE',
        status: 'CANCELADO',
        createdAt: '2026-03-09T00:00:00.000Z',
      },
    ]);
    const user = userEvent.setup();
    wrapWithAuth(<PublicHeader />);
    await user.click(screen.getByRole('button', { name: /Notificações/i }));
    await screen.findByText('Pendente');
    expect(screen.getByText('Cancelado')).toBeInTheDocument();
  });

  it('logout closes dropdown and clears auth', async () => {
    const user = userEvent.setup();
    wrapWithAuth(<PublicHeader />);
    await user.click(screen.getByRole('button', { name: /Notificações/i }));
    await screen.findByText('Meus agendamentos');
    await user.click(screen.getByRole('button', { name: /Sair/i }));
    expect(screen.queryByText('Meus agendamentos')).not.toBeInTheDocument();
  });
});
