import { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { fetchPublicServicos, fetchPublicStaff } from '@/features/admin/api';
import type { StaffResponse, HorarioFuncionamentoStaff } from '@/features/admin/api';
import { getPublicToken, setPublicToken } from '@/features/public/auth';
import { createPublicAgendamento } from '@/features/public/api';
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

function getInitialErrorFromUrl(): string | null {
  const params = new URLSearchParams(window.location.search);
  const oauthError = params.get('oauth_error');
  return oauthError ? decodeURIComponent(oauthError) : null;
}

export function AgendarPage() {
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
