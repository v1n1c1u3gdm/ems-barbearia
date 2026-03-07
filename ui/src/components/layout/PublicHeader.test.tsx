import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { PublicHeader } from './PublicHeader';

function wrap(ui: React.ReactElement) {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
}

describe('PublicHeader', () => {
  it('renders logo link to home', () => {
    wrap(<PublicHeader />);
    const logo = screen.getByRole('link', { name: /EMS Barbearia - Início/i });
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveAttribute('href', '/');
  });

  it('renders nav links Início, Serviços, Contato, Agendar', () => {
    wrap(<PublicHeader />);
    expect(screen.getByRole('link', { name: 'Início' })).toHaveAttribute('href', '#inicio');
    expect(screen.getByRole('link', { name: 'Serviços' })).toHaveAttribute('href', '#servicos');
    expect(screen.getByRole('link', { name: 'Contato' })).toHaveAttribute('href', '#contato');
    expect(screen.getByRole('link', { name: 'Agendar' })).toBeInTheDocument();
  });
});
