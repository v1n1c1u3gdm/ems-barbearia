import { Link, useNavigate } from 'react-router-dom';
import { clearStoredAuth } from '@/config/auth';

const ADMIN_AREAS = [
  { to: '/admin/contatos', label: 'Contatos' },
  { to: '/admin/promocoes', label: 'Promoções' },
  { to: '/admin/agendamentos', label: 'Agendamentos' },
  { to: '/admin/clientes', label: 'Clientes' },
] as const;

export function AdminDashboardPage() {
  const navigate = useNavigate();

  function handleLogout() {
    clearStoredAuth();
    navigate('/admin/login', { replace: true });
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-2 text-2xl font-bold text-white">Painel</h1>
      <p className="mb-6 text-zinc-400">
        Áreas administrativas:
      </p>
      <ul className="mb-8 grid gap-2 sm:grid-cols-2">
        {ADMIN_AREAS.map(({ to, label }) => (
          <li key={to}>
            <Link
              to={to}
              className="block rounded-lg border border-zinc-800 bg-zinc-900/80 px-4 py-3 text-white transition hover:border-zinc-700"
            >
              {label}
            </Link>
          </li>
        ))}
      </ul>
      <div className="flex gap-4">
        <button
          type="button"
          onClick={handleLogout}
          className="rounded-md bg-zinc-700 px-4 py-2 text-sm font-medium text-white transition hover:bg-zinc-600"
        >
          Sair
        </button>
      </div>
    </div>
  );
}
