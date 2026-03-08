import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { clearStoredAuth } from '@/config/auth';
import { fetchDashboardSummary } from '@/features/admin/api';

const ADMIN_AREAS = [
  { to: '/admin/contatos', label: 'Contatos', key: 'contatos' as const },
  { to: '/admin/promocoes', label: 'Promoções', key: 'promocoes' as const },
  { to: '/admin/agendamentos', label: 'Agendamentos', key: 'agendamentos' as const },
  { to: '/admin/clientes', label: 'Clientes', key: 'clientes' as const },
] as const;

export function AdminDashboardPage() {
  const navigate = useNavigate();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin', 'dashboard', 'summary'],
    queryFn: fetchDashboardSummary,
  });

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
      {isLoading && (
        <p className="mb-6 text-zinc-500">Carregando…</p>
      )}
      {isError && (
        <p className="mb-6 text-red-400" role="alert">
          Erro ao carregar resumo. Tente novamente.
        </p>
      )}
      <ul className="mb-8 grid gap-2 sm:grid-cols-2">
        {ADMIN_AREAS.map(({ to, label, key }) => (
          <li key={to}>
            <Link
              to={to}
              className="block rounded-lg border border-zinc-800 bg-zinc-900/80 px-4 py-3 text-white transition hover:border-zinc-700"
            >
              <span className="font-medium">{label}</span>
              {data != null && (
                <span className="ml-2 text-zinc-400">
                  ({data[key]})
                </span>
              )}
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
