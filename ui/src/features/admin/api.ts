const API_BASE = '/api';

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token?: string;
  [key: string]: unknown;
};

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
