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
  contatos: number;
  clientes: number;
  agendamentos: number;
  promocoes: number;
  ultimaAtualizacao: string | null;
};

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

export type ContatoResponse = {
  id: number;
  nome: string;
  email: string;
  telefone: string | null;
  mensagem: string | null;
  createdAt: string;
};

export type ContatoRequest = {
  nome: string;
  email: string;
  telefone?: string;
  mensagem?: string;
};

export type PromocaoResponse = {
  id: number;
  titulo: string;
  descricao: string | null;
  validoDe: string | null;
  validoAte: string | null;
  ativo: boolean;
  createdAt: string;
};

export type PromocaoRequest = {
  titulo: string;
  descricao?: string;
  validoDe?: string;
  validoAte?: string;
  ativo?: boolean;
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

export async function fetchContatos(nome?: string): Promise<ContatoResponse[]> {
  const url = nome ? `${API_BASE}/admin/contatos?nome=${encodeURIComponent(nome)}` : `${API_BASE}/admin/contatos`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar contatos');
  return res.json() as Promise<ContatoResponse[]>;
}

export async function fetchContatoById(id: number): Promise<ContatoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/contatos/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar contato');
  return res.json() as Promise<ContatoResponse>;
}

export async function createContato(body: ContatoRequest): Promise<ContatoResponse> {
  const res = await fetch(`${API_BASE}/admin/contatos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar contato');
  return res.json() as Promise<ContatoResponse>;
}

export async function updateContato(id: number, body: ContatoRequest): Promise<ContatoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/contatos/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar contato');
  return res.json() as Promise<ContatoResponse>;
}

export async function deleteContato(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/contatos/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir contato');
  return true;
}

export async function fetchPromocoes(titulo?: string): Promise<PromocaoResponse[]> {
  const url = titulo ? `${API_BASE}/admin/promocoes?titulo=${encodeURIComponent(titulo)}` : `${API_BASE}/admin/promocoes`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao listar promoções');
  return res.json() as Promise<PromocaoResponse[]>;
}

export async function fetchPromocaoById(id: number): Promise<PromocaoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/promocoes/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar promoção');
  return res.json() as Promise<PromocaoResponse>;
}

export async function createPromocao(body: PromocaoRequest): Promise<PromocaoResponse> {
  const res = await fetch(`${API_BASE}/admin/promocoes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Erro ao criar promoção');
  return res.json() as Promise<PromocaoResponse>;
}

export async function updatePromocao(id: number, body: PromocaoRequest): Promise<PromocaoResponse | null> {
  const res = await fetch(`${API_BASE}/admin/promocoes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao atualizar promoção');
  return res.json() as Promise<PromocaoResponse>;
}

export async function deletePromocao(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/admin/promocoes/${id}`, { method: 'DELETE' });
  if (res.status === 404) return false;
  if (!res.ok) throw new Error('Erro ao excluir promoção');
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
