import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PageLayout } from './PageLayout';

describe('PageLayout', () => {
  it('renders header, main outlet and footer', () => {
    render(
      <MemoryRouter>
        <Routes>
          <Route path="/" element={<PageLayout />}>
            <Route index element={<span>Child content</span>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: /EMS Barbearia - Início/i })).toBeInTheDocument();
    expect(screen.getByText('Child content')).toBeInTheDocument();
  });
});
