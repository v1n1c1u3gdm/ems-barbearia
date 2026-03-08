import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';
import { AgendarPage } from './AgendarPage';

vi.mock('@/features/admin/api', () => ({
  fetchClientes: vi.fn(() =>
    Promise.resolve([{ id: 1, nome: 'Cliente A', email: 'a@x.com', telefone: null, createdAt: '2025-01-01T00:00:00Z' }])
  ),
  fetchPublicServicos: vi.fn(() =>
    Promise.resolve([{ id: 1, titulo: 'Corte', descricao: null, validoDe: null, validoAte: null, ativo: true, duracaoMinutos: 30, createdAt: '2025-01-01T00:00:00Z' }])
  ),
  fetchPublicStaff: vi.fn(() =>
    Promise.resolve([{ id: 1, nome: 'João', ativo: true, createdAt: '2025-01-01T00:00:00Z' }])
  ),
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
}));

function wrap(ui: React.ReactNode) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      {ui}
    </QueryClientProvider>
  );
}

describe('AgendarPage', () => {
  it('renders Agendar horário heading', () => {
    wrap(<AgendarPage />);
    expect(screen.getByRole('heading', { name: 'Agendar horário' })).toBeInTheDocument();
  });

  it('renders form with cliente, servico, staff, dataHora and tipo', async () => {
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByLabelText(/Cliente/)).toBeInTheDocument();
    });
    expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Profissional/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data e hora/)).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: /Firme/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Solicitar agendamento/ })).toBeInTheDocument();
  });

  it('shows success message after submit', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await screen.findByText(/Cliente A/);
    await user.selectOptions(screen.getByLabelText(/Cliente/), '1');
    await user.selectOptions(screen.getByLabelText(/Serviço/), '1');
    await user.selectOptions(screen.getByLabelText(/Profissional/), '1');
    const dataHora = screen.getByLabelText(/Data e hora/);
    await user.type(dataHora, '2025-06-01T10:00');
    await user.click(screen.getByRole('button', { name: /Solicitar agendamento/ }));
    await waitFor(() => {
      expect(screen.getByText(/Agendamento solicitado com sucesso/)).toBeInTheDocument();
    });
  });
});
