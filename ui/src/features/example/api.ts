import { apiFetch } from '@/lib/api';
import type { ExampleItem, ExampleCreateInput } from './types';

const BASE = '/api/examples';

export async function fetchExamples(name?: string): Promise<ExampleItem[]> {
  const q = name ? `?name=${encodeURIComponent(name)}` : '';
  return apiFetch<ExampleItem[]>(`${BASE}${q}`);
}

export async function fetchExampleById(id: number): Promise<ExampleItem> {
  return apiFetch<ExampleItem>(`${BASE}/${id}`);
}

export async function createExample(data: ExampleCreateInput): Promise<ExampleItem> {
  return apiFetch<ExampleItem>(BASE, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateExample(id: number, data: ExampleCreateInput): Promise<ExampleItem> {
  return apiFetch<ExampleItem>(`${BASE}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function deleteExample(id: number): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: 'DELETE' });
}
