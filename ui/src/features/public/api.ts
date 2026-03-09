import { getPublicToken } from './auth';

const API_BASE = '/api';

export type ClientePublicResponse = {
  id: number;
  nome: string;
  email: string;
  telefone: string | null;
  createdAt: string;
};

export type PublicTokenResponse = { token: string };

export type PublicRegisterPayload = { nome: string; email: string; senha: string };
export type PublicLoginPayload = { email: string; senha: string };
export type PhoneOtpPayload = { telefone: string };
export type VerifyOtpPayload = { telefone: string; code: string };

export type PublicAgendamentoPayload = {
  servicoId: number;
  staffId: number;
  dataHora: string;
  tipo: string;
};

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
  tamanhoFilaSlot?: number | null;
};

export type PublicSlotResponse = {
  staffId: number | null;
  staffNome: string | null;
  dataHora: string;
  dataHoraFim: string | null;
  tipo: string;
  status: string;
};

export async function fetchPublicSlots(params: {
  de: string;
  ate: string;
  staffId?: number;
}): Promise<PublicSlotResponse[]> {
  const q = new URLSearchParams({ de: params.de, ate: params.ate });
  if (params.staffId != null) q.set('staffId', String(params.staffId));
  const res = await fetch(`${API_BASE}/agendamentos/slots?${q}`);
  if (!res.ok) throw new Error('Erro ao carregar agenda do dia');
  return res.json() as Promise<PublicSlotResponse[]>;
}

function authHeaders(): Record<string, string> {
  const token = getPublicToken();
  const h: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

export async function register(payload: PublicRegisterPayload): Promise<PublicTokenResponse> {
  const res = await fetch(`${API_BASE}/auth/public/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(res.status === 409 ? 'Email já cadastrado' : text || 'Erro ao cadastrar');
  }
  return res.json() as Promise<PublicTokenResponse>;
}

export async function login(payload: PublicLoginPayload): Promise<PublicTokenResponse> {
  const res = await fetch(`${API_BASE}/auth/public/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(res.status === 401 ? 'Credenciais inválidas' : 'Erro ao entrar');
  return res.json() as Promise<PublicTokenResponse>;
}

export async function fetchMe(): Promise<ClientePublicResponse | null> {
  const res = await fetch(`${API_BASE}/auth/public/me`, { headers: authHeaders() });
  if (res.status === 401 || res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao carregar perfil');
  return res.json() as Promise<ClientePublicResponse>;
}

export async function requestOtp(payload: PhoneOtpPayload): Promise<void> {
  const res = await fetch(`${API_BASE}/auth/public/phone/request-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (res.status === 429) throw new Error('Aguarde um minuto para solicitar novo código');
  if (!res.ok) throw new Error('Erro ao enviar código');
}

export async function verifyOtp(payload: VerifyOtpPayload): Promise<PublicTokenResponse> {
  const res = await fetch(`${API_BASE}/auth/public/phone/verify-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(res.status === 401 ? 'Código inválido ou expirado' : 'Erro ao verificar');
  return res.json() as Promise<PublicTokenResponse>;
}

export function getGoogleOAuthUrl(): string {
  return `${API_BASE}/auth/public/oauth/google`;
}

export function getAppleOAuthUrl(): string {
  return `${API_BASE}/auth/public/oauth/apple`;
}

export async function fetchMyAgendamentos(): Promise<AgendamentoResponse[]> {
  const res = await fetch(`${API_BASE}/agendamentos/me`, { headers: authHeaders() });
  if (res.status === 401) return [];
  if (!res.ok) throw new Error('Erro ao carregar seus agendamentos');
  return res.json() as Promise<AgendamentoResponse[]>;
}

export async function cancelPublicAgendamento(id: number): Promise<AgendamentoResponse | null> {
  const res = await fetch(`${API_BASE}/agendamentos/${id}/cancel`, {
    method: 'PATCH',
    headers: authHeaders(),
  });
  if (res.status === 403 || res.status === 404) return null;
  if (!res.ok) throw new Error('Erro ao cancelar');
  return res.json() as Promise<AgendamentoResponse>;
}

export async function createPublicAgendamento(body: PublicAgendamentoPayload): Promise<AgendamentoResponse> {
  const res = await fetch(`${API_BASE}/agendamentos`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const msg =
      res.status === 401
        ? 'Faça login para agendar'
        : res.status === 409
          ? 'Horário já ocupado para este profissional. Escolha outro horário.'
          : 'Erro ao agendar. Tente novamente.';
    throw new Error(msg);
  }
  return res.json() as Promise<AgendamentoResponse>;
}
