import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
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
    cancelPublicAgendamento: vi.fn(() => Promise.resolve(null)),
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

  it('renders calendar and slot-first message when authenticated', async () => {
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: 'Dia' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Semana' })).toBeInTheDocument();
    expect(screen.getByLabelText(/Ir para/)).toBeInTheDocument();
    expect(
      screen.getByText(/Clique em um horário disponível no calendário.*para agendar/)
    ).toBeInTheDocument();
  });

  it('shows form after clicking slot and success message after submit', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    expect(slotButtons.length).toBeGreaterThan(0);
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Serviço/), screen.getByRole('option', { name: /Corte/ }));
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    await user.click(screen.getByRole('button', { name: /Confirmar agendamento/ }));
    await waitFor(() => {
      expect(screen.getByText(/Agendamento solicitado com sucesso/)).toBeInTheDocument();
    });
  });

  it('shows confirmation form with selected date/time after clicking slot', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    expect(slotButtons.length).toBeGreaterThan(0);
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    expect(screen.getByText(/Data e horário:/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Escolher outro horário/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Confirmar agendamento/ })).toBeInTheDocument();
  });

  it('shows Cancelar in Meus agendamentos for firme aprovado', async () => {
    const { fetchMyAgendamentos } = await import('@/features/public/api');
    vi.mocked(fetchMyAgendamentos).mockResolvedValueOnce([
      {
        id: 1,
        clienteId: 10,
        clienteNome: 'Eu',
        servicoId: 1,
        servicoTitulo: 'Barba',
        staffId: 1,
        staffNome: 'Emerson',
        dataHora: '2026-03-10T14:00:00.000Z',
        dataHoraFim: null,
        tipo: 'FIRME',
        status: 'APROVADO',
        createdAt: '2026-03-09T00:00:00.000Z',
      },
    ]);
    wrap(<AgendarPage />);
    await screen.findByText('Meus agendamentos');
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument();
  });

  it('shows validation error when submitting without servico and staff', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    const dialog = screen.getByRole('dialog', { name: 'Confirmar agendamento' });
    const form = dialog.querySelector('form');
    expect(form).toBeTruthy();
    fireEvent.submit(form!);
    await waitFor(() => {
      expect(within(dialog).getByText('Preencha serviço e profissional.')).toBeInTheDocument();
    });
  });

  it('shows API error when create fails', async () => {
    const { createPublicAgendamento } = await import('@/features/public/api');
    vi.mocked(createPublicAgendamento).mockImplementationOnce(() =>
      Promise.reject(new Error('Horário indisponível'))
    );
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Serviço/), screen.getByRole('option', { name: /Corte/ }));
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    await user.click(screen.getByRole('button', { name: /Confirmar agendamento/ }));
    const dialog = screen.getByRole('dialog', { name: 'Confirmar agendamento' });
    expect(await within(dialog).findByText('Horário indisponível')).toBeInTheDocument();
  });

  it('switches to day view when clicking Dia', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: 'Dia' }));
    const dayButton = screen.getByRole('button', { name: 'Dia' });
    expect(dayButton).toHaveClass('bg-amber-500');
  });

  it('submits with tipo Encaixe', async () => {
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Serviço/), screen.getByRole('option', { name: /Corte/ }));
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    await user.click(screen.getByRole('radio', { name: 'Encaixe' }));
    await user.click(screen.getByRole('button', { name: /Confirmar agendamento/ }));
    await waitFor(() => {
      expect(screen.getByText(/Agendamento solicitado com sucesso/)).toBeInTheDocument();
    });
  });

  it('shows staff disponibilidade in modal when staff has horarios', async () => {
    const { fetchPublicStaff } = await import('@/features/admin/api');
    vi.mocked(fetchPublicStaff).mockResolvedValueOnce([
      {
        id: 1,
        nome: 'João',
        ativo: true,
        createdAt: '2025-01-01T00:00:00Z',
        horarios: [
          { diaSemana: 1, horaInicio: '09:00', horaFim: '18:00', aberto: true },
          { diaSemana: 3, horaInicio: '08:30', horaFim: '17:30', aberto: true },
        ],
      },
    ]);
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    await waitFor(() => {
      expect(screen.getByText(/Seg 09:00–18:00/)).toBeInTheDocument();
      expect(screen.getByText(/Qua 08:30–17:30/)).toBeInTheDocument();
    });
  });

  it('shows Fechado when staff horarios are all closed', async () => {
    const { fetchPublicStaff } = await import('@/features/admin/api');
    vi.mocked(fetchPublicStaff).mockResolvedValueOnce([
      {
        id: 1,
        nome: 'João',
        ativo: true,
        createdAt: '2025-01-01T00:00:00Z',
        horarios: [
          { diaSemana: 1, horaInicio: null, horaFim: null, aberto: false },
        ],
      },
    ]);
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    await waitFor(() => {
      expect(screen.getByText('Horário')).toBeInTheDocument();
    });
    const slotButtons = screen.getAllByTestId('slot-button');
    await user.click(slotButtons[0]);
    await waitFor(() => {
      expect(screen.getByLabelText(/Serviço/)).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText(/Profissional/), screen.getByRole('option', { name: /João/ }));
    await waitFor(() => {
      expect(screen.getByText(/Fechado\./)).toBeInTheDocument();
    });
  });

  it('opens modal from calendar block and shows Cancelar for firme aprovado', async () => {
    const now = new Date();
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1);
    const monday = new Date(now.getFullYear(), now.getMonth(), diff, 0, 0, 0, 0);
    const wed = new Date(monday);
    wed.setDate(monday.getDate() + 2);
    wed.setHours(14, 0, 0, 0);
    const dataHora = wed.toISOString();

    const { fetchMyAgendamentos } = await import('@/features/public/api');
    vi.mocked(fetchMyAgendamentos).mockResolvedValueOnce([
      {
        id: 1,
        clienteId: 10,
        clienteNome: 'Eu',
        servicoId: 1,
        servicoTitulo: 'Barba',
        staffId: 1,
        staffNome: 'Emerson',
        dataHora,
        dataHoraFim: null,
        tipo: 'FIRME',
        status: 'APROVADO',
        createdAt: '2026-03-09T00:00:00.000Z',
      },
    ]);
    const user = userEvent.setup();
    wrap(<AgendarPage />);
    const calendarBlock = await screen.findByRole('button', { name: /Barba/ });
    await user.click(calendarBlock);
    const dialog = screen.getByRole('dialog', { name: 'Meu agendamento' });
    expect(within(dialog).getByText('Emerson')).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: 'Cancelar' })).toBeInTheDocument();
  });
});
