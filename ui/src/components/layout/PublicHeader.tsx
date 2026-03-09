import { useQuery } from '@tanstack/react-query';
import { Bell, LogOut } from 'lucide-react';
import { useEffect,useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { fetchMyAgendamentos } from '@/features/public/api';
import { useOptionalPublicAuth } from '@/features/public/PublicAuthContext';

function formatAgendamentoDate(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function statusLabel(status: string): string {
  if (status === 'PENDENTE') return 'Pendente';
  if (status === 'APROVADO') return 'Confirmado';
  if (status === 'CANCELADO') return 'Cancelado';
  return status;
}

export function PublicHeader() {
  const navigate = useNavigate();
  const auth = useOptionalPublicAuth();
  const [notifOpen, setNotifOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const { data: myAgendamentos = [] } = useQuery({
    queryKey: ['public', 'meus-agendamentos'],
    queryFn: fetchMyAgendamentos,
    enabled: !!auth?.hasToken,
  });

  useEffect(() => {
    if (!notifOpen) return;
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setNotifOpen(false);
      }
    }
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [notifOpen]);

  const handleLogout = () => {
    auth?.clearToken();
    setNotifOpen(false);
    navigate('/agendar');
  };

  return (
    <header className="sticky top-0 z-50 border-b border-zinc-800 bg-zinc-950/95 backdrop-blur">
      <nav className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link to="/" className="flex items-center gap-2" aria-label="EMS Barbearia - Início">
          <img
            src="/tesoura-pente-ems.svg"
            alt="EMS Barbearia"
            className="h-9 w-auto"
            width="140"
            height="36"
          />
        </Link>
        <div className="flex items-center gap-6">
          <Link
            to={{ pathname: '/', hash: 'inicio' }}
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Início
          </Link>
          <Link
            to={{ pathname: '/', hash: 'servicos' }}
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Serviços
          </Link>
          <Link
            to={{ pathname: '/', hash: 'contato' }}
            className="text-sm text-zinc-300 transition hover:text-white"
          >
            Contato
          </Link>
          <Link
            to="/agendar"
            className="rounded-md bg-amber-500 px-4 py-2 text-sm font-medium text-zinc-950 transition hover:bg-amber-400"
          >
            Agendar
          </Link>
          {auth?.hasToken && (
            <div className="relative flex items-center gap-2" ref={dropdownRef}>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  setNotifOpen((o) => !o);
                }}
                className="rounded-md p-2 text-zinc-400 transition hover:bg-zinc-800 hover:text-white"
                aria-label="Notificações"
                aria-expanded={notifOpen ? 'true' : 'false'}
              >
                <Bell className="size-5" />
              </button>
              {notifOpen && (
                <div className="absolute right-0 top-full z-50 mt-1 w-80 rounded-lg border border-zinc-700 bg-zinc-900 py-2 shadow-xl">
                  <div className="border-b border-zinc-700 px-3 pb-2 text-sm font-medium text-zinc-200">
                    Meus agendamentos
                  </div>
                  {myAgendamentos.length === 0 ? (
                    <p className="px-3 py-4 text-sm text-zinc-500">Nenhum agendamento.</p>
                  ) : (
                    <ul className="max-h-72 overflow-y-auto">
                      {myAgendamentos.map((ag) => (
                        <li
                          key={ag.id}
                          className="border-b border-zinc-800 px-3 py-2 last:border-b-0"
                        >
                          <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-sm">
                            <span className="font-medium text-zinc-200 tabular-nums">
                              {formatAgendamentoDate(ag.dataHora)}
                            </span>
                            <span className="text-zinc-400">{ag.servicoTitulo ?? '—'}</span>
                            <span
                              className={
                                ag.status === 'PENDENTE'
                                  ? 'text-amber-400'
                                  : ag.status === 'APROVADO'
                                    ? 'text-emerald-400'
                                    : 'text-zinc-500'
                              }
                            >
                              {statusLabel(ag.status)}
                            </span>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                  <div className="border-t border-zinc-700 px-3 pt-2">
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="flex w-full items-center justify-center gap-2 rounded-md py-2 text-sm text-zinc-400 transition hover:bg-zinc-800 hover:text-white"
                    >
                      <LogOut className="size-4" />
                      Sair
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}
