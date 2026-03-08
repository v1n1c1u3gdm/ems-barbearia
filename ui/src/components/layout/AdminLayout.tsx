import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';

const ADMIN_NAV = [
  { to: '/admin', label: 'Painel' },
  { to: '/admin/contatos', label: 'Contatos' },
  { to: '/admin/promocoes', label: 'Promoções' },
  { to: '/admin/agendamentos', label: 'Agendamentos' },
  { to: '/admin/clientes', label: 'Clientes' },
] as const;

const PUBLIC_NAV = [
  { to: '/', hash: '#inicio', label: 'Início' },
  { to: '/', hash: '#servicos', label: 'Serviços' },
  { to: '/', hash: '#contato', label: 'Contato' },
] as const;

export function AdminLayout() {
  const location = useLocation();
  const isLoginPage = location.pathname === '/admin/login';

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      <header className="border-b border-zinc-800 bg-zinc-900/50">
        <div className="container mx-auto flex h-14 flex-wrap items-center justify-between gap-2 px-4">
          <Link
            to={isLoginPage ? '/' : '/admin'}
            className="text-lg font-semibold text-white"
          >
            EMS Barbearia{isLoginPage ? '' : ' — Admin'}
          </Link>
          <nav className="flex items-center gap-1">
            {isLoginPage
              ? PUBLIC_NAV.map(({ to, hash, label }) => (
                  <Link
                    key={hash}
                    to={`${to}${hash}`}
                    className="rounded-md px-3 py-2 text-sm text-zinc-400 transition hover:bg-zinc-800/50 hover:text-white"
                  >
                    {label}
                  </Link>
                ))
              : ADMIN_NAV.map(({ to, label }) => (
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
            {!isLoginPage && (
              <a
                href="/"
                className="ml-2 rounded-md px-3 py-2 text-sm text-zinc-400 transition hover:bg-zinc-800/50 hover:text-white"
              >
                Voltar ao site
              </a>
            )}
          </nav>
        </div>
      </header>
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
