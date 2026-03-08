import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchAgendamentos,
  fetchConfiguracaoAgenda,
  updateAgendamentoStatus,
  type AgendamentoResponse,
  type HorarioFuncionamentoResponse,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';

const ROW_HEIGHT = 40;
const DEFAULT_SLOT_MINUTES = 30;

function parseTimeToMinutes(s: string | null): number {
  if (!s) return 0;
  const parts = s.split(':').map(Number);
  return (parts[0] ?? 0) * 60 + (parts[1] ?? 0);
}

function getStartOfDay(date: Date): Date {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

function toISO(date: Date): string {
  return date.toISOString();
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

function parseISO(iso: string): Date {
  return new Date(iso);
}

function blockStyle(ag: AgendamentoResponse): string {
  if (ag.status === 'PENDENTE') return 'bg-amber-600/80 border-amber-500';
  if (ag.status === 'APROVADO') return 'bg-emerald-600/80 border-emerald-500';
  if (ag.status === 'CANCELADO') return 'bg-zinc-600/80 border-zinc-500';
  return 'bg-zinc-500/80 border-zinc-400';
}

export function AdminAgendamentosPage() {
  const queryClient = useQueryClient();
  const [viewMode, setViewMode] = useState<'day' | 'week'>('week');
  const [selectedDate, setSelectedDate] = useState(() => getStartOfDay(new Date()));
  const [modalAgendamento, setModalAgendamento] = useState<AgendamentoResponse | null>(null);

  const { de, ate } = useMemo(() => {
    if (viewMode === 'day') {
      const start = getStartOfDay(selectedDate);
      const end = new Date(start);
      end.setDate(end.getDate() + 1);
      return { de: toISO(start), ate: toISO(end) };
    }
    const { start, end } = getWeekRange(selectedDate);
    const endNext = new Date(end);
    endNext.setDate(endNext.getDate() + 1);
    return { de: toISO(start), ate: toISO(endNext) };
  }, [viewMode, selectedDate]);

  const { data: config, isLoading: configLoading } = useQuery({
    queryKey: ['admin', 'configuracao-agenda'],
    queryFn: fetchConfiguracaoAgenda,
  });

  const { data: agendamentos = [], isLoading: agendamentosLoading } = useQuery({
    queryKey: ['admin', 'agendamentos', de, ate],
    queryFn: () => fetchAgendamentos({ de, ate }),
  });

  const horarioByDia = useMemo(() => {
    const list = config?.horarios;
    if (!list?.length) return new Map<number, HorarioFuncionamentoResponse>();
    return new Map(list.map((h) => [h.diaSemana, h]));
  }, [config]);

  const slotMinutos = config?.slotMinutos ?? DEFAULT_SLOT_MINUTES;
  const isLoading = configLoading || agendamentosLoading;

  const approveMutation = useMutation({
    mutationFn: (id: number) => updateAgendamentoStatus(id, 'APROVADO'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'agendamentos'] });
      setModalAgendamento(null);
    },
  });

  const columns = viewMode === 'day' ? 1 : 7;
  const days = useMemo(() => {
    if (viewMode === 'day') {
      return [selectedDate];
    }
    const { start } = getWeekRange(selectedDate);
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      return d;
    });
  }, [viewMode, selectedDate]);

  type DayConfig = { aberto: boolean; startMinutes: number; endMinutes: number; rowCount: number };
  function getDayConfig(day: Date): DayConfig {
    const horario = horarioByDia.get(day.getDay());
    if (!horario?.aberto || horario.horaInicio == null || horario.horaFim == null) {
      return { aberto: false, startMinutes: 0, endMinutes: 0, rowCount: 0 };
    }
    const startMinutes = parseTimeToMinutes(horario.horaInicio);
    const endMinutes = parseTimeToMinutes(horario.horaFim);
    const total = endMinutes - startMinutes;
    const rowCount = total <= 0 ? 0 : Math.ceil(total / slotMinutos);
    return { aberto: true, startMinutes, endMinutes, rowCount };
  }

  const now = new Date();

  function positionFor(ag: AgendamentoResponse, dayStart: Date): { top: number; height: number; left: number; width: number } | null {
    const dayConfig = getDayConfig(dayStart);
    if (!dayConfig.aberto) return null;
    const start = parseISO(ag.dataHora);
    const end = ag.dataHoraFim ? parseISO(ag.dataHoraFim) : new Date(start.getTime() + slotMinutos * 60 * 1000);
    const dayStartStr = dayStart.toDateString();
    const startStr = start.toDateString();
    if (startStr !== dayStartStr) return null;
    const startMinutes = start.getHours() * 60 + start.getMinutes();
    const endMinutes = end.getHours() * 60 + end.getMinutes();
    if (startMinutes < dayConfig.startMinutes || endMinutes <= dayConfig.startMinutes) return null;
    if (startMinutes >= dayConfig.endMinutes) return null;
    const top = ((startMinutes - dayConfig.startMinutes) / slotMinutos) * ROW_HEIGHT;
    const height = Math.max(ROW_HEIGHT / 2, ((endMinutes - startMinutes) / slotMinutos) * ROW_HEIGHT);
    return { top, height, left: 0, width: 100 };
  }

  function currentTimePosition(day: Date): number | null {
    if (day.toDateString() !== now.toDateString()) return null;
    const dayConfig = getDayConfig(day);
    if (!dayConfig.aberto) return null;
    const nowMinutes = now.getHours() * 60 + now.getMinutes();
    if (nowMinutes < dayConfig.startMinutes || nowMinutes >= dayConfig.endMinutes) return null;
    return ((nowMinutes - dayConfig.startMinutes) / slotMinutos) * ROW_HEIGHT;
  }

  return (
    <div>
      <div className="mb-4 flex flex-wrap items-center gap-4">
        <h1 className="text-2xl font-bold text-white">Agendamentos</h1>
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
        <div className="flex items-center gap-2">
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
              ? selectedDate.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' })
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
        </div>
      </div>

      {isLoading && <p className="text-zinc-400">Carregando...</p>}

      <div className="overflow-x-auto rounded-lg border border-zinc-700 bg-zinc-900">
        <div className="flex min-w-[800px]" style={{ width: columns * 140 }}>
          {days.map((day) => {
            const dayConfig = getDayConfig(day);
            const height = dayConfig.aberto ? dayConfig.rowCount * ROW_HEIGHT : ROW_HEIGHT;
            return (
              <div key={day.toISOString()} className="flex-1 border-r border-zinc-700 last:border-r-0" style={{ minWidth: 140 }}>
                <div className="sticky top-0 z-10 border-b border-zinc-700 bg-zinc-800 py-2 text-center text-sm font-medium text-zinc-200">
                  {formatDateBR(day)}
                </div>
                <div className="relative" style={{ height }}>
                  {dayConfig.aberto ? (
                    <>
                      {Array.from({ length: dayConfig.rowCount }, (_, i) => (
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
                      {agendamentos.map((ag) => {
                        const pos = positionFor(ag, day);
                        if (!pos) return null;
                        return (
                          <button
                            key={ag.id}
                            type="button"
                            onClick={() => setModalAgendamento(ag)}
                            className={`absolute left-1 right-1 z-10 overflow-hidden rounded border text-left text-xs ${blockStyle(ag)}`}
                            style={{
                              top: pos.top,
                              height: pos.height,
                              minHeight: 20,
                            }}
                          >
                            <span className="block truncate font-medium">{ag.servicoTitulo ?? '—'}</span>
                            <span className="block truncate text-white/90">{ag.clienteNome}</span>
                            <span className="block truncate text-white/70">{ag.staffNome ?? '—'}</span>
                            <span className="block truncate text-white/70">{ag.status}</span>
                          </button>
                        );
                      })}
                    </>
                  ) : (
                    <div className="flex h-full items-center justify-center text-sm text-zinc-500">
                      Fechado
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
        <div className="flex border-t border-zinc-700" style={{ width: columns * 140 }}>
          {days.map((day) => {
            const dayConfig = getDayConfig(day);
            const label = dayConfig.aberto
              ? `${Math.floor(dayConfig.startMinutes / 60)}h – ${Math.floor(dayConfig.endMinutes / 60)}h`
              : 'Fechado';
            return (
              <div key={day.toISOString()} className="flex-1 border-r border-zinc-700 py-1 text-center text-xs text-zinc-500 last:border-r-0" style={{ minWidth: 140 }}>
                {label}
              </div>
            );
          })}
        </div>
      </div>

      <Modal
        open={modalAgendamento != null}
        onClose={() => setModalAgendamento(null)}
        title="Agendamento"
      >
        {modalAgendamento && (
          <div className="space-y-3">
            <p><span className="text-zinc-500">Cliente:</span> {modalAgendamento.clienteNome}</p>
            <p><span className="text-zinc-500">Serviço:</span> {modalAgendamento.servicoTitulo ?? '—'}</p>
            <p><span className="text-zinc-500">Profissional:</span> {modalAgendamento.staffNome ?? '—'}</p>
            <p><span className="text-zinc-500">Data/hora:</span> {parseISO(modalAgendamento.dataHora).toLocaleString('pt-BR')}</p>
            <p><span className="text-zinc-500">Tipo:</span> {modalAgendamento.tipo}</p>
            <p><span className="text-zinc-500">Status:</span> {modalAgendamento.status}</p>
            {modalAgendamento.status === 'PENDENTE' && (
              <button
                type="button"
                onClick={() => approveMutation.mutate(modalAgendamento.id)}
                disabled={approveMutation.isPending}
                className="rounded bg-emerald-600 px-4 py-2 font-medium text-white hover:bg-emerald-500 disabled:opacity-50"
              >
                {approveMutation.isPending ? 'Aprovando...' : 'Aprovar'}
              </button>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
