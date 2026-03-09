import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';

import { getPublicToken } from '@/features/public/auth';
import { PublicAuthProvider } from '@/features/public/PublicAuthContext';

import { AgendarPage } from './AgendarPage';

vi.mock('@/features/public/auth', () => ({
  getPublicToken: vi.fn(() => 'fake-token'),
  setPublicToken: vi.fn(),
}));

vi.mock('@/features/public/api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/features/public/api')>();
  return {
    ...actual,
    createPublicAgendamento: vi.fn(() =>
      Promise.resolve({
        id: 1,
        clienteId: 1,
        clienteNome: 'Cliente A',
        servicoId: 1,
        servicoTitulo: 'Corte',
        staffId: 1,
        staffNome: 'João',
        dataHora: '2025-06-01T10:00:00Z',
        dataHoraFim: '2025-06-01T10:30:00Z',
        tipo: 'FIRME',
        status: 'PENDENTE',
        createdAt: '2025-01-01T00:00:00Z',
      })
    ),
    fetchMyAgendamentos: vi.fn(() => Promise.resolve([])),
    fetchPublicSlots: vi.fn(() => Promise.resolve([])),
  };
});

vi.mock('@/features/admin/api', () => ({
  fetchPublicServicos: vi.fn(() =>
    Promise.resolve([
      {
        id: 1,
        titulo: 'Corte',
        descricao: null,
        validoDe: null,
        validoAte: null,
        ativo: true,
        duracaoMinutos: 30,
        createdAt: '2025-01-01T00:00:00Z',
      },
    ])
  ),
  fetchPublicStaff: vi.fn(() =>
    Promise.resolve([{ id: 1, nome: 'João', ativo: true, createdAt: '2025-01-01T00:00:00Z', horarios: [] }])
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
      <PublicAuthProvider>{ui}</PublicAuthProvider>
    </QueryClientProvider>
  );
}

describe('AgendarPage', () => {
  it('renders Agendar horário heading', () => {
    wrap(<AgendarPage />);
    expect(screen.getByRole('heading', { name: 'Agendar horário' })).toBeInTheDocument();
  });

  it('shows auth gate when not authenticated', () => {
    vi.mocked(getPublicToken).mockReturnValueOnce(null);
    wrap(<AgendarPage />);
    expect(screen.getByText(/Entrar ou cadastrar/)).toBeInTheDocument();
    expect(screen.getByText(/Identifique-se para solicitar/)).toBeInTheDocument();
  });

  it('renders booking form when authenticated', async () => {
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    expect(screen.getByLabelText(/Profissional/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data e hora/)).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: /Firme/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Solicitar agendamento/ })).toBeInTheDocument();
  });

  it('shows success message after submit', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await screen.findByLabelText(/Serviço/);
    await waitFor(() => {
      expect(screen.getByRole('option', { name: /Corte/ })).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Serviço/), screen.getByRole('option', { name: /Corte/ }));
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    const dataHora = screen.getByLabelText(/Data e hora/);
    await user.type(dataHora, '2025-06-01T10:00');
    await user.click(screen.getByRole('button', { name: /Solicitar agendamento/ }));
    await waitFor(() => {
      expect(screen.getByText(/Agendamento solicitado com sucesso/)).toBeInTheDocument();
    });
  });

  it('shows day agenda with slots when date is set', async () => {
    const { fetchPublicSlots } = await import('@/features/public/api');
    vi.mocked(fetchPublicSlots).mockResolvedValueOnce([
      {
        staffId: 1,
        staffNome: 'João',
        dataHora: '2025-06-01T10:00:00.000Z',
        dataHoraFim: '2025-06-01T10:30:00.000Z',
        tipo: 'FIRME',
        status: 'PENDENTE',
      },
      {
        staffId: 1,
        staffNome: 'João',
        dataHora: '2025-06-01T14:00:00.000Z',
        dataHoraFim: null,
        tipo: 'ENCAIXE',
        status: 'APROVADO',
      },
    ]);
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await screen.findByLabelText(/Serviço/);
    const dataHoraInput = screen.getByLabelText(/Data e hora/);
    await user.clear(dataHoraInput);
    await user.type(dataHoraInput, '2025-06-01T10:00');
    await waitFor(() => {
      expect(screen.getByText('Agenda do dia')).toBeInTheDocument();
    });
    expect(screen.getByRole('heading', { name: 'Agenda do dia' })).toBeInTheDocument();
    expect(screen.getByText('Pendente')).toBeInTheDocument();
    expect(screen.getByText('Aprovado')).toBeInTheDocument();
  });
});
