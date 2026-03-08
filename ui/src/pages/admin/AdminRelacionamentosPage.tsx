import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchRelacionamentos,
  fetchRelacionamentoById,
  updateRelacionamentoStatus,
  fetchAgendamentos,
  type RelacionamentoResponse,
  type CanalRelacionamento,
  type StatusRelacionamento,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';
import { ArrowLeft, Eye, MessageCircle, Mail, Instagram } from 'lucide-react';

const CANAIS: { key: CanalRelacionamento; label: string; description: string; icon: typeof Mail }[] = [
  { key: 'WHATSAPP', label: 'WhatsApp', description: 'Relacionamentos por WhatsApp', icon: MessageCircle },
  { key: 'EMAIL', label: 'E-mail', description: 'Relacionamentos por e-mail', icon: Mail },
  { key: 'INSTAGRAM', label: 'Instagram', description: 'Relacionamentos por Instagram', icon: Instagram },
];

const STATUS_LABEL: Record<StatusRelacionamento, string> = {
  QUENTE: 'Quente',
  MORNO: 'Morno',
  FRIO: 'Frio',
  GELADO: 'Gelado',
};

const STATUS_OPTIONS: StatusRelacionamento[] = ['QUENTE', 'MORNO', 'FRIO', 'GELADO'];

function formatDate(iso: string | null) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

function StatusBadge({ status }: { status: StatusRelacionamento }) {
  const colors: Record<StatusRelacionamento, string> = {
    QUENTE: 'bg-red-900/50 text-red-200',
    MORNO: 'bg-amber-900/50 text-amber-200',
    FRIO: 'bg-sky-900/50 text-sky-200',
    GELADO: 'bg-zinc-600 text-zinc-200',
  };
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-medium ${colors[status]}`}>
      {STATUS_LABEL[status]}
    </span>
  );
}

const tableStyles = {
  table: { style: { backgroundColor: 'rgb(24 24 27)' } },
  headRow: { style: { backgroundColor: 'rgb(39 39 42)', minHeight: '40px' } },
  headCells: { style: { color: 'rgb(228 228 231)', fontSize: '0.875rem' } },
  rows: { style: { color: 'rgb(228 228 231)', backgroundColor: 'rgb(24 24 27)' } },
  pagination: { style: { backgroundColor: 'rgb(24 24 27)', color: 'rgb(228 228 231)' } },
};

export function AdminRelacionamentosPage() {
  const queryClient = useQueryClient();
  const [selectedCanal, setSelectedCanal] = useState<CanalRelacionamento | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [showHistorico, setShowHistorico] = useState(false);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'relacionamentos', selectedCanal],
    queryFn: () => fetchRelacionamentos(selectedCanal!, undefined),
    enabled: selectedCanal != null,
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'relacionamentos', selectedId],
    queryFn: () => fetchRelacionamentoById(selectedId!),
    enabled: selectedId != null,
  });

  const { data: agendamentos = [], isLoading: loadingHistorico } = useQuery({
    queryKey: ['admin', 'agendamentos', selected?.clienteId],
    queryFn: () => fetchAgendamentos({ clienteId: selected!.clienteId! }),
    enabled: showHistorico && selected?.clienteId != null,
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: StatusRelacionamento }) =>
      updateRelacionamentoStatus(id, status),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'relacionamentos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'relacionamentos', id] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
    },
  });

  const openDetail = useCallback((row: RelacionamentoResponse) => {
    setSelectedId(row.id);
    setShowHistorico(false);
  }, []);

  const closeDetail = useCallback(() => {
    setSelectedId(null);
    setShowHistorico(false);
  }, []);

  const handleStatusChange = useCallback(
    (id: number, status: StatusRelacionamento) => {
      updateStatusMutation.mutate({ id, status });
    },
    [updateStatusMutation]
  );

  const columns = [
    { name: 'Nome', selector: (r: RelacionamentoResponse) => r.nome, sortable: true },
    {
      name: 'Contato',
      selector: (r: RelacionamentoResponse) => (r.canal === 'EMAIL' ? r.email : r.telefone ?? r.email),
      sortable: false,
    },
    {
      name: 'Status',
      cell: (row: RelacionamentoResponse) => <StatusBadge status={row.status} />,
      sortable: true,
    },
    {
      name: 'Última interação',
      selector: (r: RelacionamentoResponse) => r.dataUltimaInteracao ?? '—',
      cell: (r: RelacionamentoResponse) => formatDate(r.dataUltimaInteracao),
      sortable: false,
    },
    {
      name: 'Tipo',
      selector: (r: RelacionamentoResponse) => r.tipoInteracao,
      cell: (r: RelacionamentoResponse) =>
        r.tipoInteracao === 'MOTIVADA_PELO_CLIENTE' ? 'Cliente' : 'Sistema',
      sortable: false,
    },
    {
      name: 'Ações',
      cell: (row: RelacionamentoResponse) => (
        <button
          type="button"
          onClick={() => openDetail(row)}
          className="rounded p-1.5 bg-zinc-600 text-white hover:bg-zinc-500"
          title="Ver"
          aria-label="Ver"
        >
          <Eye className="size-4" />
        </button>
      ),
      ignoreRowClick: true,
      allowOverflow: true,
      button: true,
    },
  ];

  if (selectedCanal == null) {
    return (
      <div>
        <h1 className="mb-6 text-2xl font-bold text-white">Relacionamentos</h1>
        <p className="mb-6 text-zinc-400">
          Selecione um canal para ver os relacionamentos.
        </p>
        <div className="grid max-w-3xl grid-cols-1 gap-6 sm:grid-cols-3">
          {CANAIS.map(({ key, label, description, icon: Icon }) => (
            <button
              key={key}
              type="button"
              onClick={() => setSelectedCanal(key)}
              className="flex flex-col items-start gap-4 rounded-2xl border border-zinc-700 bg-zinc-900/80 p-5 text-left transition hover:border-zinc-600 hover:shadow-md"
            >
              <div className="flex size-10 items-center justify-center rounded-full bg-zinc-800">
                <Icon className="size-6 text-zinc-300" />
              </div>
              <div>
                <h3 className="text-sm font-medium text-white">{label}</h3>
                <p className="mt-1 text-sm text-zinc-400">{description}</p>
              </div>
            </button>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4 flex items-center gap-4">
        <button
          type="button"
          onClick={() => setSelectedCanal(null)}
          className="flex items-center gap-1 rounded p-2 text-zinc-400 hover:bg-zinc-800 hover:text-white"
          aria-label="Voltar aos canais"
        >
          <ArrowLeft className="size-4" />
          <span className="text-sm">Canais</span>
        </button>
        <h1 className="text-2xl font-bold text-white">
          Relacionamentos — {CANAIS.find((c) => c.key === selectedCanal)?.label}
        </h1>
      </div>

      {isError && (
        <p className="mb-4 text-red-400" role="alert">
          Erro ao carregar relacionamentos.
        </p>
      )}

      <DataTable
        columns={columns}
        data={list}
        progressPending={isLoading}
        customStyles={tableStyles}
        pagination
        paginationPerPage={10}
        noDataComponent="Nenhum relacionamento neste canal."
      />

      <Modal open={selectedId != null} onClose={closeDetail} title="Detalhe do relacionamento">
        {selected && (
          <div className="space-y-4">
            <dl className="grid gap-2 sm:grid-cols-2">
              <div>
                <dt className="text-sm text-zinc-500">Nome</dt>
                <dd className="text-white">{selected.nome}</dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">E-mail</dt>
                <dd className="text-white">{selected.email}</dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">Telefone</dt>
                <dd className="text-white">{selected.telefone ?? '—'}</dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">Canal</dt>
                <dd className="text-white">{selected.canal}</dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">Data última interação</dt>
                <dd className="text-white">{formatDate(selected.dataUltimaInteracao)}</dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">Tipo interação</dt>
                <dd className="text-white">
                  {selected.tipoInteracao === 'MOTIVADA_PELO_CLIENTE' ? 'Cliente' : 'Sistema'}
                </dd>
              </div>
              <div>
                <dt className="text-sm text-zinc-500">Criado em</dt>
                <dd className="text-white">{formatDate(selected.createdAt)}</dd>
              </div>
            </dl>

            <div>
              <label htmlFor="rel-status" className="mb-1 block text-sm text-zinc-400">
                Status
              </label>
              <select
                id="rel-status"
                value={selected.status}
                onChange={(e) =>
                  handleStatusChange(selected.id, e.target.value as StatusRelacionamento)
                }
                disabled={updateStatusMutation.isPending}
                className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white disabled:opacity-50"
              >
                {STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>
                    {STATUS_LABEL[s]}
                  </option>
                ))}
              </select>
            </div>

            {selected.clienteId != null && (
              <div>
                <button
                  type="button"
                  onClick={() => setShowHistorico(!showHistorico)}
                  className="rounded bg-zinc-600 px-4 py-2 text-sm text-white hover:bg-zinc-500"
                >
                  {showHistorico ? 'Ocultar histórico' : 'Histórico de consumo'}
                </button>
                {showHistorico && (
                  <div className="mt-3 rounded border border-zinc-700 bg-zinc-900/50 p-3">
                    {loadingHistorico ? (
                      <p className="text-sm text-zinc-400">Carregando…</p>
                    ) : agendamentos.length === 0 ? (
                      <p className="text-sm text-zinc-400">Nenhum agendamento.</p>
                    ) : (
                      <ul className="space-y-2 text-sm">
                        {agendamentos.map((a) => (
                          <li key={a.id} className="flex flex-wrap gap-x-4 gap-y-1 text-zinc-300">
                            <span>{formatDate(a.dataHora)}</span>
                            <span>{a.servicoTitulo ?? '—'}</span>
                            <span className="text-zinc-500">{a.status}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
