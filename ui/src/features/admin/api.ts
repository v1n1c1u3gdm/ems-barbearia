const API_BASE = '/api';

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token?: string;
  [key: string]: unknown;
};

export type DashboardSummary = {
  relacionamentos: number;
  clientes: number;
  agendamentos: number;
  servicos: number;
  ultimaAtualizacao: string | null;
};

export type CanalRelacionamento = 'WHATSAPP' | 'EMAIL' | 'INSTAGRAM';
export type StatusRelacionamento = 'QUENTE' | 'MORNO' | 'FRIO' | 'GELADO';
export type TipoInteracao = 'MOTIVADA_PELO_CLIENTE' | 'MOTIVADA_PELO_SISTEMA';

export type RelacionamentoResponse = {
  id: number;
  nome: string;
  email: string;
  telefone: string | null;
  canal: CanalRelacionamento;
  status: StatusRelacionamento;
  dataUltimaInteracao: string | null;
  tipoInteracao: TipoInteracao;
  clienteId: number | null;
  createdAt: string;
};

export async function fetchRelacionamentos(canal?: CanalRelacionamento, status?: StatusRelacionamento): Promise<RelacionamentoResponse[]> {
  const params = new URLSearchParams();
  if (canal) params.set('canal', canal);
  if (status) params.set('status', status);
  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`${API_BASE}/admin/relacionamentos${query}`);
  if (!res.ok) throw new Error('Erro ao listar relacionamentos');
  return res.json() as Promise<RelacionamentoResponse[]>;
}

export async function fetchRelacionamentoById(id: number): Promise<RelacionamentoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/relacionamentos/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar relacionamento');
  return res.json() as Promise<RelacionamentoResponse>;
}

export async function updateRelacionamentoStatus(id: number, status: StatusRelacionamento): Promise<RelacionamentoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/relacionamentos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar status');
  return res.json() as Promise<RelacionamentoResponse>;
}

export async function fetchDashboardSummary(): Promise<DashboardSummary> {
  const res = await fetch(`${API_BASE}/admin/dashboard/summary`);
  if (!res.ok) {
    throw new Error('Erro ao carregar resumo');
  }
  return res.json() as Promise<DashboardSummary>;
}

export async function login(
  payload: LoginPayload
): Promise<LoginResponse> {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'Credenciais inválidas' : 'Erro ao entrar');
  }
  return res.json() as Promise<LoginResponse>;
}

export type ServicoResponse = {
  id: number;
  titulo: string;
  descricao: string | null;
  validoDe: string | null;
  validoAte: string | null;
  ativo: boolean;
  duracaoMinutos: number | null;
  createdAt: string;
};

export type ServicoRequest = {
  titulo: string;
  descricao?: string;
  validoDe?: string;
  validoAte?: string;
  ativo?: boolean;
  duracaoMinutos?: number;
};

export type ClienteResponse = {
  id: number;
  nome: string;
  email: string;
  telefone: string | null;
  createdAt: string;
};

export type ClienteRequest = {
  nome: string;
  email: string;
  telefone?: string;
};

export async function fetchServicos(titulo?: string): Promise<ServicoResponse[]> {
  const url = titulo ? `${API_BASE}/admin/servicos?titulo=${encodeURIComponent(titulo)}` : `${API_BASE}/admin/servicos`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar serviços');
  return res.json() as Promise<ServicoResponse[]>;
}

export async function fetchServicoById(id: number): Promise<ServicoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/servicos/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar serviço');
  return res.json() as Promise<ServicoResponse>;
}

export async function createServico(body: ServicoRequest): Promise<ServicoResponse> {
  const res = await fetch(`${API_BASE}/admin/servicos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar serviço');
  return res.json() as Promise<ServicoResponse>;
}

export async function updateServico(id: number, body: ServicoRequest): Promise<ServicoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/servicos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar serviço');
  return res.json() as Promise<ServicoResponse>;
}

export async function deleteServico(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/servicos/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir serviço');
  return true;
}

export async function fetchClientes(nome?: string): Promise<ClienteResponse[]> {
  const url = nome ? `${API_BASE}/admin/clientes?nome=${encodeURIComponent(nome)}` : `${API_BASE}/admin/clientes`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar clientes');
  return res.json() as Promise<ClienteResponse[]>;
}

export async function fetchClienteById(id: number): Promise<ClienteResponse | null> {
  const res = await fetch(`${API_BASE}/admin/clientes/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar cliente');
  return res.json() as Promise<ClienteResponse>;
}

export async function createCliente(body: ClienteRequest): Promise<ClienteResponse> {
  const res = await fetch(`${API_BASE}/admin/clientes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar cliente');
  return res.json() as Promise<ClienteResponse>;
}

export async function updateCliente(id: number, body: ClienteRequest): Promise<ClienteResponse | null> {
  const res = await fetch(`${API_BASE}/admin/clientes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar cliente');
  return res.json() as Promise<ClienteResponse>;
}

export async function deleteCliente(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/clientes/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir cliente');
  return true;
}

export type HorarioFuncionamentoStaff = {
  diaSemana: number;
  aberto: boolean;
  horaInicio: string | null;
  horaFim: string | null;
};

export type StaffResponse = {
  id: number;
  nome: string;
  ativo: boolean;
  createdAt: string;
  horarios?: HorarioFuncionamentoStaff[];
};

export type HorarioFuncionamentoRequest = {
  diaSemana: number;
  aberto: boolean;
  horaInicio?: string | null;
  horaFim?: string | null;
};

export type StaffRequest = {
  nome: string;
  ativo?: boolean;
  horarios?: HorarioFuncionamentoRequest[] | null;
};

export async function fetchStaff(nome?: string): Promise<StaffResponse[]> {
  const url = nome ? `${API_BASE}/admin/staff?nome=${encodeURIComponent(nome)}` : `${API_BASE}/admin/staff`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar staff');
  return res.json() as Promise<StaffResponse[]>;
}

export async function fetchStaffById(id: number): Promise<StaffResponse | null> {
  const res = await fetch(`${API_BASE}/admin/staff/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar staff');
  return res.json() as Promise<StaffResponse>;
}

export async function createStaff(body: StaffRequest): Promise<StaffResponse> {
  const res = await fetch(`${API_BASE}/admin/staff`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar staff');
  return res.json() as Promise<StaffResponse>;
}

export async function updateStaff(id: number, body: StaffRequest): Promise<StaffResponse | null> {
  const res = await fetch(`${API_BASE}/admin/staff/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar staff');
  return res.json() as Promise<StaffResponse>;
}

export async function deleteStaff(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/staff/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir staff');
  return true;
}

export type ServicoSummary = { id: number; titulo: string };

export type AssinaturaResponse = {
  id: number;
  clienteId: number;
  clienteNome: string;
  servicos: ServicoSummary[];
  createdAt: string;
};

export type AssinaturaRequest = {
  clienteId: number;
  servicoIds: number[];
};

export async function fetchAssinaturas(clienteId?: number): Promise<AssinaturaResponse[]> {
  const url = clienteId != null ? `${API_BASE}/admin/assinaturas?clienteId=${clienteId}` : `${API_BASE}/admin/assinaturas`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar assinaturas');
  return res.json() as Promise<AssinaturaResponse[]>;
}

export async function fetchAssinaturaById(id: number): Promise<AssinaturaResponse | null> {
  const res = await fetch(`${API_BASE}/admin/assinaturas/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar assinatura');
  return res.json() as Promise<AssinaturaResponse>;
}

export async function createAssinatura(body: AssinaturaRequest): Promise<AssinaturaResponse> {
  const res = await fetch(`${API_BASE}/admin/assinaturas`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar assinatura');
  return res.json() as Promise<AssinaturaResponse>;
}

export async function updateAssinatura(id: number, body: AssinaturaRequest): Promise<AssinaturaResponse | null> {
  const res = await fetch(`${API_BASE}/admin/assinaturas/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar assinatura');
  return res.json() as Promise<AssinaturaResponse>;
}

export async function deleteAssinatura(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/assinaturas/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir assinatura');
  return true;
}

export type AgendamentoResponse = {
  id: number;
  clienteId: number;
  clienteNome: string;
  servicoId: number | null;
  servicoTitulo: string | null;
  staffId: number | null;
  staffNome: string | null;
  dataHora: string;
  dataHoraFim: string | null;
  tipo: string;
  status: string;
  createdAt: string;
};

export type AgendamentoRequest = {
  clienteId: number;
  servicoId: number;
  staffId: number;
  dataHora: string;
  tipo: string;
  status?: string;
};

function agendamentosQueryParams(params: { clienteId?: number; staffId?: number; status?: string; de?: string; ate?: string }): string {
  const sp = new URLSearchParams();
  if (params.clienteId != null) sp.set('clienteId', String(params.clienteId));
  if (params.staffId != null) sp.set('staffId', String(params.staffId));
  if (params.status != null && params.status !== '') sp.set('status', params.status);
  if (params.de != null) sp.set('de', params.de);
  if (params.ate != null) sp.set('ate', params.ate);
  const q = sp.toString();
  return q ? `?${q}` : '';
}

export async function fetchAgendamentos(params?: { clienteId?: number; staffId?: number; status?: string; de?: string; ate?: string }): Promise<AgendamentoResponse[]> {
  const query = params ? agendamentosQueryParams(params) : '';
  const res = await fetch(`${API_BASE}/admin/agendamentos${query}`);
  if (!res.ok) throw new Error('Erro ao listar agendamentos');
  return res.json() as Promise<AgendamentoResponse[]>;
}

export async function fetchAgendamentoById(id: number): Promise<AgendamentoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/agendamentos/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar agendamento');
  return res.json() as Promise<AgendamentoResponse>;
}

export async function createAgendamento(body: AgendamentoRequest): Promise<AgendamentoResponse> {
  const res = await fetch(`${API_BASE}/admin/agendamentos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(res.status === 409 ? 'Horário já ocupado para este profissional' : 'Erro ao criar agendamento');
  return res.json() as Promise<AgendamentoResponse>;
}

export async function updateAgendamento(id: number, body: AgendamentoRequest): Promise<AgendamentoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/agendamentos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(res.status === 409 ? 'Horário já ocupado para este profissional' : 'Erro ao atualizar agendamento');
  return res.json() as Promise<AgendamentoResponse>;
}

export async function deleteAgendamento(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/agendamentos/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir agendamento');
  return true;
}

export type HorarioFuncionamentoResponse = {
  diaSemana: number;
  aberto: boolean;
  horaInicio: string | null;
  horaFim: string | null;
};

export type ConfiguracaoAgendaResponse = {
  slotMinutos: number;
  horarios: HorarioFuncionamentoResponse[];
};

export async function fetchConfiguracaoAgenda(): Promise<ConfiguracaoAgendaResponse> {
  const res = await fetch(`${API_BASE}/admin/configuracao-agenda`);
  if (!res.ok) throw new Error('Erro ao carregar configuração da agenda');
  return res.json() as Promise<ConfiguracaoAgendaResponse>;
}

export async function updateAgendamentoStatus(id: number, status: string): Promise<AgendamentoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/agendamentos/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar status');
  return res.json() as Promise<AgendamentoResponse>;
}

export async function fetchPublicServicos(): Promise<ServicoResponse[]> {
  const res = await fetch(`${API_BASE}/servicos`);
  if (!res.ok) throw new Error('Erro ao carregar serviços');
  return res.json() as Promise<ServicoResponse[]>;
}

export async function fetchPublicStaff(): Promise<StaffResponse[]> {
  const res = await fetch(`${API_BASE}/staff`);
  if (!res.ok) throw new Error('Erro ao carregar profissionais');
  return res.json() as Promise<StaffResponse[]>;
}

export async function createPublicAgendamento(body: AgendamentoRequest): Promise<AgendamentoResponse> {
  const res = await fetch(`${API_BASE}/agendamentos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const msg = res.status === 409 ? 'Horário já ocupado para este profissional. Escolha outro horário.' : 'Erro ao agendar. Tente novamente.';
    throw new Error(msg);
  }
  return res.json() as Promise<AgendamentoResponse>;
}

export type ProverbioResponse = {
  referencia: string;
  texto: string;
};

export async function fetchProverbioRandom(): Promise<ProverbioResponse | null> {
  const res = await fetch(`${API_BASE}/proverbios/random`);
  if (!res.ok) return null;
  return res.json() as Promise<ProverbioResponse>;
}
