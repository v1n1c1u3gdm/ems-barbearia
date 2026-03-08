const STORAGE_KEY = 'public_auth_token';

export function getPublicToken(): string | null {
  return sessionStorage.getItem(STORAGE_KEY);
}

export function setPublicToken(token: string): void {
  sessionStorage.setItem(STORAGE_KEY, token);
}

export function clearPublicToken(): void {
  sessionStorage.removeItem(STORAGE_KEY);
}
