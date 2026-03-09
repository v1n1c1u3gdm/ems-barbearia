import { useState, useEffect, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchPublicServicos, fetchPublicStaff } from '@/features/admin/api';
import type { StaffResponse, HorarioFuncionamentoStaff } from '@/features/admin/api';
import { getPublicToken, setPublicToken } from '@/features/public/auth';
import { createPublicAgendamento, fetchMyAgendamentos, fetchPublicSlots } from '@/features/public/api';
import { AgendarAuthGate } from '@/features/public/AgendarAuthGate';

function formatDisponibilidade(horarios: HorarioFuncionamentoStaff[] | undefined): string {
  if (!horarios?.length) return 'Sem disponibilidade cadastrada.';
  const parts: string[] = [];
  const dias = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  for (const h of horarios) {
    if (!h.aberto || h.horaInicio == null || h.horaFim == null) continue;
    const hi = h.horaInicio.slice(0, 5);
    const hf = h.horaFim.slice(0, 5);
    parts.push(`${dias[h.diaSemana]} ${hi}–${hf}`);
  }
  if (parts.length === 0) return 'Fechado.';
  return parts.join('; ');
}

function toISO(dataHoraLocal: string): string {
  if (!dataHoraLocal) return '';
  return new Date(dataHoraLocal).toISOString();
}

function dayRangeFromDateInput(dataHoraLocal: string): { de: string; ate: string } | null {
  if (!dataHoraLocal || dataHoraLocal.length < 10) return null;
  const d = new Date(dataHoraLocal);
  const start = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0, 0);
  const end = new Date(start);
  end.setDate(end.getDate() + 1);
  return { de: start.toISOString(), ate: end.toISOString() };
}

function formatSlotTime(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function getInitialErrorFromUrl(): string | null {
  const params = new URLSearchParams(window.location.search);
  const oauthError = params.get('oauth_error');
  return oauthError ? decodeURIComponent(oauthError) : null;
}

export function AgendarPage() {
  const queryClient = useQueryClient();
  const [hasToken, setHasToken] = useState<boolean>(() => !!getPublicToken());
  const [servicoId, setServicoId] = useState<string>('');
  const [staffId, setStaffId] = useState<string>('');
  const [dataHora, setDataHora] = useState<string>('');
  const [tipo, setTipo] = useState<string>('FIRME');
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(getInitialErrorFromUrl);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      setPublicToken(token);
      window.history.replaceState({}, '', window.location.pathname);
      queueMicrotask(() => setHasToken(true));
    }
    if (params.get('oauth_error')) {
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  const { data: servicos = [] } = useQuery({
    queryKey: ['public', 'servicos'],
    queryFn: () => fetchPublicServicos(),
  });

  const { data: staffList = [] } = useQuery({
    queryKey: ['public', 'staff'],
    queryFn: () => fetchPublicStaff(),
  });

  const { data: myAgendamentos = [], isLoading: myAgendamentosLoading } = useQuery({
    queryKey: ['public', 'meus-agendamentos'],
    queryFn: fetchMyAgendamentos,
    enabled: hasToken,
  });

  const dayRange = useMemo(() => dayRangeFromDateInput(dataHora), [dataHora]);
  const staffIdNum = staffId ? Number(staffId) : undefined;
  const { data: daySlots = [], isLoading: daySlotsLoading } = useQuery({
    queryKey: ['public', 'slots', dayRange?.de, dayRange?.ate, staffIdNum],
    queryFn: () =>
      fetchPublicSlots({
        de: dayRange!.de,
        ate: dayRange!.ate,
        staffId: staffIdNum,
      }),
    enabled: !!dayRange?.de && !!dayRange?.ate,
  });

  const createMutation = useMutation({
    mutationFn: (body: { servicoId: number; staffId: number; dataHora: string; tipo: string }) =>
      createPublicAgendamento(body),
    onSuccess: () => {
      setSuccess(true);
      setError(null);
      setServicoId('');
      setStaffId('');
      setDataHora('');
      setTipo('FIRME');
      queryClient.invalidateQueries({ queryKey: ['public', 'slots'] });
      queryClient.invalidateQueries({ queryKey: ['public', 'meus-agendamentos'] });
    },
    onError: (err: Error) => {
      setError(err.message);
      setSuccess(false);
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    const sId = Number(servicoId);
    const stId = Number(staffId);
    if (!sId || !stId || !dataHora.trim()) {
      setError('Preencha todos os campos.');
      return;
    }
    createMutation.mutate({
      servicoId: sId,
      staffId: stId,
      dataHora: toISO(dataHora),
      tipo: tipo || 'FIRME',
    });
  }

  if (!hasToken) {
    return (
      <div className="mx-auto max-w-xl px-4 py-12">
        <h1 className="mb-8 text-3xl font-bold text-zinc-100">Agendar horário</h1>
        {error && (
          <div className="mb-6 rounded-lg border border-red-500/50 bg-red-500/10 px-4 py-3 text-red-400">
            {error}
          </div>
        )}
        <p className="mb-6 text-zinc-400">Identifique-se para solicitar seu agendamento.</p>
        <AgendarAuthGate onAuthenticated={() => setHasToken(true)} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-xl px-4 py-12">
      <h1 className="mb-8 text-3xl font-bold text-zinc-100">Agendar horário</h1>

      {myAgendamentosLoading ? (
        <p className="mb-6 text-sm text-zinc-500">Carregando seus agendamentos…</p>
      ) : myAgendamentos.length > 0 ? (
        <section className="mb-8 rounded-lg border border-zinc-700 bg-zinc-900/80 p-4">
          <h2 className="mb-3 text-lg font-semibold text-zinc-200">Meus agendamentos</h2>
          <ul className="space-y-2">
            {myAgendamentos.map((ag) => (
              <li
                key={ag.id}
                className="flex flex-wrap items-center gap-x-3 gap-y-1 rounded border border-zinc-700 bg-zinc-800/80 px-3 py-2 text-sm"
              >
                <span className="font-medium text-zinc-200">
                  {new Date(ag.dataHora).toLocaleString('pt-BR', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
                <span className="text-zinc-400">{ag.servicoTitulo ?? '—'}</span>
                <span className="text-zinc-400">{ag.staffNome ?? '—'}</span>
                <span
                  className={
                    ag.status === 'PENDENTE'
                      ? 'rounded bg-amber-500/20 px-1.5 py-0.5 text-xs text-amber-400'
                      : ag.status === 'APROVADO'
                        ? 'rounded bg-emerald-500/20 px-1.5 py-0.5 text-xs text-emerald-400'
                        : ag.status === 'CANCELADO'
                          ? 'rounded bg-zinc-600/50 px-1.5 py-0.5 text-xs text-zinc-400'
                          : 'rounded bg-zinc-600/50 px-1.5 py-0.5 text-xs text-zinc-400'
                  }
                >
                  {ag.status}
                </span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      {success && (
        <div className="mb-6 rounded-lg border border-emerald-500/50 bg-emerald-500/10 px-4 py-3 text-emerald-400">
          Agendamento solicitado com sucesso! Entraremos em contato para confirmar.
        </div>
      )}

      {error && (
        <div className="mb-6 rounded-lg border border-red-500/50 bg-red-500/10 px-4 py-3 text-red-400">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label htmlFor="servico" className="mb-1 block text-sm font-medium text-zinc-300">
            Serviço
          </label>
          <select
            id="servico"
            value={servicoId}
            onChange={(e) => setServicoId(e.target.value)}
            className="w-full rounded-md border border-zinc-600 bg-zinc-900 px-3 py-2 text-zinc-100 focus:border-amber-500 focus:outline-none focus:ring-1 focus:ring-amber-500"
            required
          >
            <option value="">Selecione o serviço</option>
            {servicos.map((s) => (
              <option key={s.id} value={s.id}>
                {s.titulo}
                {s.duracaoMinutos != null ? ` (${s.duracaoMinutos} min)` : ''}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="staff" className="mb-1 block text-sm font-medium text-zinc-300">
            Profissional
          </label>
          <select
            id="staff"
            value={staffId}
            onChange={(e) => setStaffId(e.target.value)}
            className="w-full rounded-md border border-zinc-600 bg-zinc-900 px-3 py-2 text-zinc-100 focus:border-amber-500 focus:outline-none focus:ring-1 focus:ring-amber-500"
            required
          >
            <option value="">Selecione o profissional</option>
            {staffList.map((s) => (
              <option key={s.id} value={s.id}>
                {s.nome}
              </option>
            ))}
          </select>
          {staffId &&
            (() => {
              const staff = staffList.find((s) => String(s.id) === staffId) as StaffResponse | undefined;
              return staff ? (
                <p className="mt-1 text-xs text-zinc-500">
                  Disponível: {formatDisponibilidade(staff.horarios)}
                </p>
              ) : null;
            })()}
        </div>

        <div>
          <label htmlFor="dataHora" className="mb-1 block text-sm font-medium text-zinc-300">
            Data e hora
          </label>
          <input
            id="dataHora"
            type="datetime-local"
            value={dataHora}
            onChange={(e) => setDataHora(e.target.value)}
            className="w-full rounded-md border border-zinc-600 bg-zinc-900 px-3 py-2 text-zinc-100 focus:border-amber-500 focus:outline-none focus:ring-1 focus:ring-amber-500"
            required
          />
        </div>

        {dayRange && (
          <div className="rounded-lg border border-zinc-700 bg-zinc-900/80 p-4">
            <h3 className="mb-3 text-sm font-semibold text-zinc-200">
              Agenda do dia
              {staffId ? ' (este profissional)' : ''}
            </h3>
            {daySlotsLoading ? (
              <p className="text-sm text-zinc-500">Carregando…</p>
            ) : daySlots.length === 0 ? (
              <p className="text-sm text-zinc-500">Nenhum agendamento neste dia.</p>
            ) : (
              <ul className="space-y-2">
                {daySlots.map((slot, i) => (
                  <li
                    key={i}
                    className="flex flex-wrap items-center gap-x-3 gap-y-1 rounded border border-zinc-700 bg-zinc-800/80 px-3 py-2 text-sm"
                  >
                    <span className="font-medium text-zinc-200 tabular-nums">
                      {formatSlotTime(slot.dataHora)}
                      {slot.dataHoraFim ? ` – ${formatSlotTime(slot.dataHoraFim)}` : ''}
                    </span>
                    {slot.staffNome && (
                      <span className="text-zinc-400">{slot.staffNome}</span>
                    )}
                    <span
                      className={
                        slot.tipo === 'FIRME'
                          ? 'rounded bg-amber-500/20 px-1.5 py-0.5 text-xs text-amber-400'
                          : 'rounded bg-zinc-600/50 px-1.5 py-0.5 text-xs text-zinc-400'
                      }
                    >
                      {slot.tipo === 'FIRME' ? 'Firme' : 'Encaixe'}
                    </span>
                    {slot.status === 'PENDENTE' && (
                      <span className="text-amber-500/90">Pendente</span>
                    )}
                    {slot.status === 'APROVADO' && (
                      <span className="text-emerald-500/90">Aprovado</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
            <p className="mt-3 text-xs text-zinc-500">
              <strong>Firme:</strong> horário fixo — escolha um horário livre. <strong>Encaixe:</strong> pode ser
              encaixado entre outros agendamentos.
            </p>
          </div>
        )}

        <div>
          <span className="mb-2 block text-sm font-medium text-zinc-300">Tipo</span>
          <div className="flex gap-4">
            <label className="flex items-center gap-2 text-zinc-300">
              <input
                type="radio"
                name="tipo"
                value="FIRME"
                checked={tipo === 'FIRME'}
                onChange={() => setTipo('FIRME')}
                className="text-amber-500 focus:ring-amber-500"
              />
              Firme (horário fixo)
            </label>
            <label className="flex items-center gap-2 text-zinc-300">
              <input
                type="radio"
                name="tipo"
                value="ENCAIXE"
                checked={tipo === 'ENCAIXE'}
                onChange={() => setTipo('ENCAIXE')}
                className="text-amber-500 focus:ring-amber-500"
              />
              Encaixe
            </label>
          </div>
        </div>

        <button
          type="submit"
          disabled={createMutation.isPending}
          className="mt-4 rounded-md bg-amber-500 px-6 py-3 font-medium text-zinc-950 transition hover:bg-amber-400 disabled:opacity-50"
        >
          {createMutation.isPending ? 'Enviando...' : 'Solicitar agendamento'}
        </button>
      </form>
    </div>
  );
}
