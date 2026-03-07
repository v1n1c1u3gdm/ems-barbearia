import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { PublicFooter } from './PublicFooter';

function wrap(ui: React.ReactElement) {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
}

describe('PublicFooter', () => {
  it('does not render Admin link', () => {
    wrap(<PublicFooter />);
    expect(screen.queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument();
  });

  it('renders full address and Instagram link', () => {
    wrap(<PublicFooter />);
    expect(screen.getByText(/Rua Doutor Romeo Ferro, 612 — Jardim Bonfiglioli, São Paulo/)).toBeInTheDocument();
    const instagram = screen.getByRole('link', { name: /Instagram EMS Barbearia/i });
    expect(instagram).toHaveAttribute('href', 'https://www.instagram.com/emsbarbearia/?hl=en');
  });

  it('renders current year in copyright', () => {
    wrap(<PublicFooter />);
    const year = new Date().getFullYear();
    expect(screen.getByText(new RegExp(`©\\s*${year}`))).toBeInTheDocument();
  });
});
