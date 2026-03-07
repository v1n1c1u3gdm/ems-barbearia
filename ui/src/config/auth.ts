export const AUTH_STORAGE_KEY = 'auth_token';

export function getStoredAuth(): string | null {
  return sessionStorage.getItem(AUTH_STORAGE_KEY);
}

export function setStoredAuth(value: string): void {
  sessionStorage.setItem(AUTH_STORAGE_KEY, value);
}

export function clearStoredAuth(): void {
  sessionStorage.removeItem(AUTH_STORAGE_KEY);
}

export function isAuthenticated(): boolean {
  return !!getStoredAuth();
}
