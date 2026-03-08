import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { PublicFooter } from './PublicFooter';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

function wrap(ui: React.ReactElement) {
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>{ui}</MemoryRouter>
    </QueryClientProvider>
  );
}

describe('PublicFooter', () => {
  it('does not render Admin link', () => {
    wrap(<PublicFooter />);
    expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument();
  });

  it('renders full address inside address tag and Instagram link', () => {
    wrap(<PublicFooter />);
    const addressEl = screen.getByRole('contentinfo').querySelector('address');
    expect(addressEl).toBeInTheDocument();
    expect(addressEl).toHaveTextContent(/Rua Doutor Romeo Ferro, 612 — Jardim Bonfiglioli, São Paulo/);
    const instagram = screen.getByRole('link', { name: /Instagram EMS Barbearia/i });
    expect(instagram).toHaveAttribute('href', 'https://www.instagram.com/emsbarbearia/?hl=en');
  });

  it('renders current year in copyright', () => {
    wrap(<PublicFooter />);
    const year = new Date().getFullYear();
    expect(screen.getByText(new RegExp(`©\\s*${year}`))).toBeInTheDocument();
  });
});
