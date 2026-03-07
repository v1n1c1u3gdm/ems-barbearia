import { render, screen } from '@testing-library/react';
import { LandingPage } from './LandingPage';

describe('LandingPage', () => {
  it('renders main heading', () => {
    render(<LandingPage />);
    expect(
      screen.getByRole('heading', { name: /barbearia para quem dita o próprio ritmo/i })
    ).toBeInTheDocument();
  });

  it('renders Nossos serviços section', () => {
    render(<LandingPage />);
    expect(screen.getByRole('heading', { name: 'Nossos serviços' })).toBeInTheDocument();
  });

  it('renders Agende seu horário section', () => {
    render(<LandingPage />);
    expect(screen.getByRole('heading', { name: 'Agende seu horário' })).toBeInTheDocument();
  });
});
