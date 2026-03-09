import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi } from 'vitest';

import { PublicFooter } from './PublicFooter';

function makeQueryClient(preloadedProverbio?: { referencia: string; texto: string }) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  if (preloadedProverbio) {
    queryClient.setQueryData(['public', 'proverbio-random'], preloadedProverbio);
  }
  return queryClient;
}

function wrap(ui: React.ReactElement, queryClient?: QueryClient) {
  const client = queryClient ?? makeQueryClient();
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>{ui}</MemoryRouter>
    </QueryClientProvider>
  );
}

describe('PublicFooter', () => {
  it('does not render Admin link', () => {
    wrap(<PublicFooter />, makeQueryClient());
    expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument();
  });

  it('renders full address inside address tag and Instagram link', () => {
    wrap(<PublicFooter />, makeQueryClient());
    const addressEl = screen.getByRole('contentinfo').querySelector('address');
    expect(addressEl).toBeInTheDocument();
    expect(addressEl).toHaveTextContent(/Rua Doutor Romeo Ferro, 612 — Jardim Bonfiglioli, São Paulo/);
    const instagram = screen.getByRole('link', { name: /Instagram EMS Barbearia/i });
    expect(instagram).toHaveAttribute('href', 'https://www.instagram.com/emsbarbearia/?hl=en');
  });

  it('renders copyright from 2024 to current year', () => {
    wrap(<PublicFooter />, makeQueryClient());
    const year = new Date().getFullYear();
    const footer = screen.getByRole('contentinfo');
    const copyrightText = footer.textContent ?? '';
    expect(copyrightText).toContain('©');
    expect(copyrightText).toContain('2024');
    expect(copyrightText).toContain('EMS Barbearia');
    if (year > 2024) {
      expect(copyrightText).toContain(String(year));
    }
  });

  it('renders proverbio when query returns data', () => {
    const proverbio = { referencia: 'Prov. 1', texto: 'Frase do provérbio' };
    wrap(<PublicFooter />, makeQueryClient(proverbio));
    expect(screen.getByText(/Frase do provérbio/)).toBeInTheDocument();
    expect(screen.getByText('Prov. 1')).toBeInTheDocument();
  });

  it('renders only start year in copyright when current year equals start year', () => {
    const getFullYear = vi.spyOn(Date.prototype, 'getFullYear').mockReturnValue(2024);
    wrap(<PublicFooter />, makeQueryClient());
    const footer = screen.getByRole('contentinfo');
    expect(footer.textContent).toContain('© 2024 EMS Barbearia');
    getFullYear.mockRestore();
  });
});
