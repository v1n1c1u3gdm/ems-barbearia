import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PageLayout } from './PageLayout';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

describe('PageLayout', () => {
  it('renders header, main outlet and footer', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <Routes>
            <Route path="/" element={<PageLayout />}>
              <Route index element={<span>Child content</span>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
    expect(screen.getByRole('link', { name: /EMS Barbearia - Início/i })).toBeInTheDocument();
    expect(screen.getByText('Child content')).toBeInTheDocument();
  });
});
