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
