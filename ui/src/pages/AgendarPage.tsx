import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';

import { Modal } from '@/components/ui/Modal';
import type { HorarioFuncionamentoStaff, StaffResponse } from '@/features/admin/api';
import { fetchPublicServicos, fetchPublicStaff } from '@/features/admin/api';
import { AgendarAuthGate } from '@/features/public/AgendarAuthGate';
import type { AgendamentoResponse } from '@/features/public/api';
import {
  cancelPublicAgendamento,
  createPublicAgendamento,
  fetchMyAgendamentos,
  fetchPublicSlots,
} from '@/features/public/api';
import { getPublicToken } from '@/features/public/auth';
import { usePublicAuth } from '@/features/public/PublicAuthContext';

const ROW_HEIGHT = 40;
const DEFAULT_SLOT_MINUTES = 30;
const DEFAULT_HORA_INICIO = '08:00';
const DEFAULT_HORA_FIM = '18:00';

function getStartOfDay(date: Date): Date {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

function getWeekRange(selectedDate: Date): { start: Date; end: Date } {
  const d = new Date(selectedDate);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  const monday = new Date(d);
  monday.setDate(diff);
  monday.setHours(0, 0, 0, 0);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  sunday.setHours(23, 59, 59, 999);
  return { start: monday, end: sunday };
}

function formatDateBR(d: Date): string {
  return d.toLocaleDateString('pt-BR', { weekday: 'short', day: '2-digit', month: 'short' });
}

function parseTimeToMinutes(s: string | null): number {
  if (!s) return 0;
  const parts = s.split(':').map(Number);
  return (parts[0] ?? 0) * 60 + (parts[1] ?? 0);
}

function calendarBlockStyle(ag: AgendamentoResponse): string {
  if (ag.status === 'PENDENTE') return 'bg-amber-600/80 border-amber-500';
  if (ag.status === 'APROVADO') return 'bg-emerald-600/80 border-emerald-500';
  if (ag.status === 'CANCELADO') return 'bg-zinc-600/80 border-zinc-500';
  return 'bg-zinc-500/80 border-zinc-400';
}

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

function statusLabel(status: string): string {
  if (status === 'PENDENTE') return 'Pendente';
  if (status === 'APROVADO') return 'Confirmado';
  if (status === 'CANCELADO') return 'Cancelado';
  return status;
}

function getInitialErrorFromUrl(): string | null {
  const params = new URLSearchParams(window.location.search);
  const oauthError = params.get('oauth_error');
  return oauthError ? decodeURIComponent(oauthError) : null;
}

export function AgendarPage() {
  const queryClient = useQueryClient();
  const { hasToken, setToken } = usePublicAuth();
  const [servicoId, setServicoId] = useState<string>('');
  const [staffId, setStaffId] = useState<string>('');
  const [dataHora, setDataHora] = useState<string>('');
  const [tipo, setTipo] = useState<string>('FIRME');
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(getInitialErrorFromUrl);
  const [viewMode, setViewMode] = useState<'day' | 'week'>('week');
  const [selectedDate, setSelectedDate] = useState(() => getStartOfDay(new Date()));
  const [modalAgendamento, setModalAgendamento] = useState<AgendamentoResponse | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      setToken(token);
      window.history.replaceState({}, '', window.location.pathname);
    }
    if (params.get('oauth_error')) {
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, [setToken]);

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

  const cancelMutation = useMutation({
    mutationFn: cancelPublicAgendamento,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['public', 'meus-agendamentos'] });
      setModalAgendamento(null);
    },
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

  const { de: calendarDe, ate: calendarAte } = useMemo(() => {
    if (viewMode === 'day') {
      const start = getStartOfDay(selectedDate);
      const end = new Date(start);
      end.setDate(end.getDate() + 1);
      return { de: start.toISOString(), ate: end.toISOString() };
    }
    const { start, end } = getWeekRange(selectedDate);
    const endNext = new Date(end);
    endNext.setDate(endNext.getDate() + 1);
    return { de: start.toISOString(), ate: endNext.toISOString() };
  }, [viewMode, selectedDate]);

  const agendamentosInRange = useMemo(() => {
    return myAgendamentos.filter((ag) => {
      const t = new Date(ag.dataHora).getTime();
      return t >= new Date(calendarDe).getTime() && t < new Date(calendarAte).getTime();
    });
  }, [myAgendamentos, calendarDe, calendarAte]);

  const defaultStartMinutes = parseTimeToMinutes(DEFAULT_HORA_INICIO);
  const defaultEndMinutes = parseTimeToMinutes(DEFAULT_HORA_FIM);
  const slotMinutos = DEFAULT_SLOT_MINUTES;
  const globalRowCount = Math.ceil((defaultEndMinutes - defaultStartMinutes) / slotMinutos);

  const days = useMemo(() => {
    if (viewMode === 'day') return [selectedDate];
    const { start } = getWeekRange(selectedDate);
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      return d;
    });
  }, [viewMode, selectedDate]);

  const openDays = days;

  type DayConfig = { aberto: boolean; startMinutes: number; endMinutes: number; rowCount: number };
  function getDayConfig(day: Date): DayConfig {
    void day;
    return {
      aberto: true,
      startMinutes: defaultStartMinutes,
      endMinutes: defaultEndMinutes,
      rowCount: globalRowCount,
    };
  }

  function formatSlotMinutes(m: number): string {
    const h = Math.floor(m / 60);
    const min = m % 60;
    return `${h.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;
  }

  const now = new Date();

  function positionFor(
    ag: AgendamentoResponse,
    dayStart: Date
  ): { top: number; height: number; left: number; width: number } | null {
    const dayConfig = getDayConfig(dayStart);
    if (!dayConfig.aberto) return null;
    const start = new Date(ag.dataHora);
    const end = ag.dataHoraFim
      ? new Date(ag.dataHoraFim)
      : new Date(start.getTime() + slotMinutos * 60 * 1000);
    if (start.toDateString() !== dayStart.toDateString()) return null;
    const startMinutes = start.getHours() * 60 + start.getMinutes();
    const endMinutes = end.getHours() * 60 + end.getMinutes();
    if (startMinutes < dayConfig.startMinutes || endMinutes <= dayConfig.startMinutes) return null;
    if (startMinutes >= dayConfig.endMinutes) return null;
    const top = ((startMinutes - defaultStartMinutes) / slotMinutos) * ROW_HEIGHT;
    const height = Math.max(
      ROW_HEIGHT / 2,
      ((endMinutes - startMinutes) / slotMinutos) * ROW_HEIGHT
    );
    return { top, height, left: 0, width: 100 };
  }

  function currentTimePosition(day: Date): number | null {
    if (day.toDateString() !== now.toDateString()) return null;
    const nowMinutes = now.getHours() * 60 + now.getMinutes();
    if (nowMinutes < defaultStartMinutes || nowMinutes >= defaultEndMinutes) return null;
    return ((nowMinutes - defaultStartMinutes) / slotMinutos) * ROW_HEIGHT;
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
        <AgendarAuthGate onAuthenticated={() => setToken(getPublicToken()!)} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-12">
      <h1 className="mb-8 text-3xl font-bold text-zinc-100">Agendar horário</h1>

      <div className="mb-8">
        <div className="mb-4 flex flex-wrap items-center gap-4">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setViewMode('day')}
              className={`rounded px-3 py-1 text-sm font-medium ${viewMode === 'day' ? 'bg-amber-500 text-zinc-950' : 'bg-zinc-700 text-zinc-300 hover:bg-zinc-600'}`}
            >
              Dia
            </button>
            <button
              type="button"
              onClick={() => setViewMode('week')}
              className={`rounded px-3 py-1 text-sm font-medium ${viewMode === 'week' ? 'bg-amber-500 text-zinc-950' : 'bg-zinc-700 text-zinc-300 hover:bg-zinc-600'}`}
            >
              Semana
            </button>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => {
                const d = new Date(selectedDate);
                d.setDate(d.getDate() - (viewMode === 'day' ? 1 : 7));
                setSelectedDate(d);
              }}
              className="rounded border border-zinc-600 bg-zinc-800 px-3 py-1 text-zinc-300 hover:bg-zinc-700"
            >
              ←
            </button>
            <span className="min-w-[200px] text-zinc-300">
              {viewMode === 'day'
                ? selectedDate.toLocaleDateString('pt-BR', {
                    weekday: 'long',
                    day: '2-digit',
                    month: 'long',
                  })
                : (() => {
                    const { start, end } = getWeekRange(selectedDate);
                    return `${start.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })} – ${end.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })}`;
                  })()}
            </span>
            <button
              type="button"
              onClick={() => {
                const d = new Date(selectedDate);
                d.setDate(d.getDate() + (viewMode === 'day' ? 1 : 7));
                setSelectedDate(d);
              }}
              className="rounded border border-zinc-600 bg-zinc-800 px-3 py-1 text-zinc-300 hover:bg-zinc-700"
            >
              →
            </button>
            <label className="flex items-center gap-2 text-sm text-zinc-400">
              Ir para:
              <input
                type="date"
                value={`${selectedDate.getFullYear()}-${String(selectedDate.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`}
                onChange={(e) => {
                  const v = e.target.value;
                  if (!v) return;
                  const [y, m, d] = v.split('-').map(Number);
                  setSelectedDate(getStartOfDay(new Date(y, m - 1, d)));
                }}
                className="rounded border border-zinc-600 bg-zinc-800 px-2 py-1 text-zinc-200"
              />
            </label>
          </div>
        </div>

        <div className="overflow-x-auto rounded-lg border border-zinc-700 bg-zinc-900">
          <div
            className="flex min-w-[800px]"
            style={{ width: 56 + openDays.length * 140 }}
          >
            <div className="shrink-0 border-r border-zinc-700" style={{ width: 56 }}>
              <div className="sticky top-0 z-10 border-b border-zinc-700 bg-zinc-800 py-2 text-center text-xs font-medium text-zinc-400">
                Horário
              </div>
              <div style={{ height: globalRowCount * ROW_HEIGHT }}>
                {Array.from({ length: globalRowCount }, (_, i) => (
                  <div
                    key={i}
                    className="flex items-center border-b border-zinc-800 pl-1.5 text-xs text-zinc-500"
                    style={{ height: ROW_HEIGHT }}
                  >
                    {formatSlotMinutes(defaultStartMinutes + i * slotMinutos)}
                  </div>
                ))}
              </div>
            </div>
            {openDays.map((day) => {
              const height = globalRowCount * ROW_HEIGHT;
              return (
                <div
                  key={day.toISOString()}
                  className="flex-1 border-r border-zinc-700 last:border-r-0"
                  style={{ minWidth: 140 }}
                >
                  <div className="sticky top-0 z-10 border-b border-zinc-700 bg-zinc-800 py-2 text-center text-sm font-medium text-zinc-200">
                    {formatDateBR(day)}
                  </div>
                  <div className="relative" style={{ height }}>
                    {Array.from({ length: globalRowCount }, (_, i) => (
                      <div
                        key={i}
                        className="border-b border-zinc-800"
                        style={{ height: ROW_HEIGHT }}
                      />
                    ))}
                    {currentTimePosition(day) != null && (
                      <div
                        className="absolute left-0 right-0 z-20 h-0.5 bg-red-500"
                        style={{ top: currentTimePosition(day)! }}
                      />
                    )}
                    {agendamentosInRange.map((ag) => {
                      const pos = positionFor(ag, day);
                      if (!pos) return null;
                      return (
                        <button
                          key={ag.id}
                          type="button"
                          onClick={() => setModalAgendamento(ag)}
                          className={`absolute left-1 right-1 z-10 overflow-hidden rounded border text-left text-xs ${calendarBlockStyle(ag)}`}
                          style={{
                            top: pos.top,
                            height: pos.height,
                            minHeight: 20,
                          }}
                        >
                          <span className="block truncate font-medium">
                            {ag.servicoTitulo ?? '—'}
                          </span>
                          <span className="block truncate text-white/90">
                            {ag.staffNome ?? '—'}
                          </span>
                          <span className="block truncate text-white/70">
                            {statusLabel(ag.status)}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
          <div
            className="flex border-t border-zinc-700"
            style={{ width: 56 + openDays.length * 140 }}
          >
            <div
              className="shrink-0 border-r border-zinc-700 py-1 text-center text-xs text-zinc-500"
              style={{ width: 56 }}
            />
            {openDays.map((day) => {
              const dayConfig = getDayConfig(day);
              const label = `${Math.floor(dayConfig.startMinutes / 60)}h – ${Math.floor(dayConfig.endMinutes / 60)}h`;
              return (
                <div
                  key={day.toISOString()}
                  className="flex-1 border-r border-zinc-700 py-1 text-center text-xs text-zinc-500 last:border-r-0"
                  style={{ minWidth: 140 }}
                >
                  {label}
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <Modal
        open={modalAgendamento != null}
        onClose={() => setModalAgendamento(null)}
        title="Meu agendamento"
      >
        {modalAgendamento && (
          <div className="space-y-3">
            <p>
              <span className="text-zinc-500">Serviço:</span>{' '}
              {modalAgendamento.servicoTitulo ?? '—'}
            </p>
            <p>
              <span className="text-zinc-500">Profissional:</span>{' '}
              {modalAgendamento.staffNome ?? '—'}
            </p>
            <p>
              <span className="text-zinc-500">Data/hora:</span>{' '}
              {new Date(modalAgendamento.dataHora).toLocaleString('pt-BR')}
            </p>
            <p>
              <span className="text-zinc-500">Status:</span>{' '}
              {statusLabel(modalAgendamento.status)}
            </p>
            {modalAgendamento.status === 'APROVADO' &&
              (modalAgendamento.tipo === 'FIRME' || !modalAgendamento.tipo) && (
                <button
                  type="button"
                  onClick={() => cancelMutation.mutate(modalAgendamento.id)}
                  disabled={cancelMutation.isPending}
                  className="rounded bg-red-600 px-4 py-2 font-medium text-white hover:bg-red-500 disabled:opacity-50"
                >
                  {cancelMutation.isPending ? 'Cancelando…' : 'Cancelar'}
                </button>
              )}
          </div>
        )}
      </Modal>

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
                  {statusLabel(ag.status)}
                </span>
                {ag.status === 'APROVADO' &&
                  (ag.tipo === 'FIRME' || !ag.tipo) && (
                    <button
                      type="button"
                      onClick={() => cancelMutation.mutate(ag.id)}
                      disabled={cancelMutation.isPending}
                      className="ml-auto text-xs text-red-400 hover:underline disabled:opacity-50"
                    >
                      {cancelMutation.isPending && cancelMutation.variables === ag.id
                        ? 'Cancelando…'
                        : 'Cancelar'}
                    </button>
                  )}
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
