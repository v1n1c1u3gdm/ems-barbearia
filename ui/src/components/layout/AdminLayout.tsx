import { Link, Outlet } from 'react-router-dom';

export function AdminLayout() {
  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      <header className="border-b border-zinc-800 bg-zinc-900/50">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link
            to="/admin"
            className="text-lg font-semibold text-white"
          >
            EMS Barbearia — Admin
          </Link>
          <div className="flex items-center gap-4">
            <a
              href="/"
              className="text-sm text-zinc-400 transition hover:text-white"
            >
              Voltar ao site
            </a>
          </div>
        </div>
      </header>
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
