import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { clearStoredAuth } from '@/config/auth';

const ADMIN_NAV_MIDDLE = [
  { to: '/admin/agendamentos', label: 'Agendamentos' },
  { to: '/admin/assinaturas', label: 'Assinaturas' },
  { to: '/admin/clientes', label: 'Clientes' },
  { to: '/admin/contatos', label: 'Contatos' },
  { to: '/admin/servicos', label: 'Serviços' },
  { to: '/admin/staff', label: 'Staff' },
] as const;

const PUBLIC_NAV = [
  { to: '/', hash: '#inicio', label: 'Início' },
  { to: '/', hash: '#servicos', label: 'Serviços' },
  { to: '/', hash: '#contato', label: 'Contato' },
] as const;

export function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const isLoginPage = location.pathname === '/admin/login';

  function handleLogout() {
    clearStoredAuth();
    navigate('/admin/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-100">
      <header className="border-b border-zinc-800 bg-zinc-900/50">
        <div className="container mx-auto flex h-14 flex-wrap items-center justify-between gap-2 px-4">
          <Link
            to={isLoginPage ? '/' : '/admin'}
            className="flex items-center gap-2"
            aria-label={isLoginPage ? 'EMS Barbearia - Início' : 'EMS Barbearia - Admin'}
          >
            <img
              src="/tesoura-pente-ems.svg"
              alt=""
              className="h-9 w-auto"
              width="140"
              height="36"
            />
            {!isLoginPage && (
              <span className="text-lg font-semibold text-white">— Admin</span>
            )}
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
              : (
                  <>
                    <NavLink
                      to="/admin"
                      end
                      className={({ isActive }) =>
                        `rounded-md px-3 py-2 text-sm transition ${
                          isActive
                            ? 'bg-zinc-800 text-white'
                            : 'text-zinc-400 hover:bg-zinc-800/50 hover:text-white'
                        }`
                      }
                    >
                      Painel
                    </NavLink>
                    {ADMIN_NAV_MIDDLE.map(({ to, label }) => (
                      <NavLink
                        key={to}
                        to={to}
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
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="rounded-md px-3 py-2 text-sm text-zinc-400 transition hover:bg-zinc-800/50 hover:text-white"
                    >
                      Sair
                    </button>
                  </>
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
