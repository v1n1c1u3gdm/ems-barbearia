import { Link, NavLink, Outlet } from 'react-router-dom';

const ADMIN_NAV = [
  { to: '/admin', label: 'Painel' },
  { to: '/admin/contatos', label: 'Contatos' },
  { to: '/admin/promocoes', label: 'Promoções' },
  { to: '/admin/agendamentos', label: 'Agendamentos' },
  { to: '/admin/clientes', label: 'Clientes' },
] as const;

export function AdminLayout() {
  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      <header className="border-b border-zinc-800 bg-zinc-900/50">
        <div className="container mx-auto flex h-14 flex-wrap items-center justify-between gap-2 px-4">
          <Link to="/admin" className="text-lg font-semibold text-white">
            EMS Barbearia — Admin
          </Link>
          <nav className="flex items-center gap-1">
            {ADMIN_NAV.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/admin'}
                className={({ isActive }) =>
                  `rounded-md px-3 py-2 text-sm transition ${
                    isActive
                      ? 'bg-zinc-800 text-white'
                      : 'text-zinc-400 hover:bg-zinc-800/50 hover:text-white'
                  }`
                }
              >
                {label}
              </NavLink>
            ))}
            <a
              href="/"
              className="ml-2 rounded-md px-3 py-2 text-sm text-zinc-400 transition hover:bg-zinc-800/50 hover:text-white"
            >
              Voltar ao site
            </a>
          </nav>
        </div>
      </header>
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
