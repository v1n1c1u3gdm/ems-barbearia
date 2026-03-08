import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchDashboardSummary } from '@/features/admin/api';

const ADMIN_AREAS = [
  { to: '/admin/agendamentos', label: 'Agendamentos', key: 'agendamentos' as const },
  { to: '/admin/clientes', label: 'Clientes', key: 'clientes' as const },
  { to: '/admin/contatos', label: 'Contatos', key: 'contatos' as const },
  { to: '/admin/servicos', label: 'Serviços', key: 'servicos' as const },
] as const;

function formatUltimaAtualizacao(iso: string | null): string {
  if (!iso) return '—';
  try {
    const d = new Date(iso);
    return d.toLocaleString('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    });
  } catch {
    return iso;
  }
}

export function AdminDashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin', 'dashboard', 'summary'],
    queryFn: fetchDashboardSummary,
  });

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
      {data != null && data.ultimaAtualizacao != null && (
        <p className="mb-4 text-sm text-zinc-500">
          Última atualização (qualquer área): {formatUltimaAtualizacao(data.ultimaAtualizacao)}
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
    </div>
  );
}
