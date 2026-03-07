import { useNavigate } from 'react-router-dom';
import { clearStoredAuth } from '@/config/auth';

export function AdminDashboardPage() {
  const navigate = useNavigate();

  function handleLogout() {
    clearStoredAuth();
    navigate('/admin/login', { replace: true });
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-2 text-2xl font-bold text-white">Painel</h1>
      <p className="mb-8 text-zinc-400">
        Área restrita — em breve mais funcionalidades.
      </p>
      <div className="flex gap-4">
        <a
          href="/"
          className="rounded-md border border-zinc-700 px-4 py-2 text-sm text-zinc-300 transition hover:border-zinc-600 hover:text-white"
        >
          Voltar ao site
        </a>
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
