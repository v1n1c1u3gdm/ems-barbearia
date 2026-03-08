import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchServicos,
  fetchServicoById,
  createServico,
  updateServico,
  deleteServico,
  type ServicoResponse,
  type ServicoRequest,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react';

const emptyForm: ServicoRequest = {
  titulo: '',
  descricao: '',
  validoDe: '',
  validoAte: '',
  ativo: true,
  duracaoMinutos: 30,
};

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

function formatLocalDate(iso: string | null) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('pt-BR');
  } catch {
    return iso;
  }
}

const tableStyles = {
  table: { style: { backgroundColor: 'rgb(24 24 27)' } },
  headRow: { style: { backgroundColor: 'rgb(39 39 42)', minHeight: '40px' } },
  headCells: { style: { color: 'rgb(228 228 231)', fontSize: '0.875rem' } },
  rows: { style: { color: 'rgb(228 228 231)', backgroundColor: 'rgb(24 24 27)' } },
  pagination: { style: { backgroundColor: 'rgb(24 24 27)', color: 'rgb(228 228 231)' } },
};

export function AdminServicosPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<ServicoRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'servicos'],
    queryFn: () => fetchServicos(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'servicos', selectedId],
    queryFn: () => fetchServicoById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createServico,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'servicos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: ServicoRequest }) => updateServico(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'servicos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'servicos', selectedId] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteServico,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'servicos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: ServicoResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: ServicoResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({
        titulo: row.titulo,
        descricao: row.descricao ?? '',
        validoDe: row.validoDe ?? '',
        validoAte: row.validoAte ?? '',
        ativo: row.ativo,
        duracaoMinutos: row.duracaoMinutos ?? 30,
      });
    } else {
      setSelectedId(null);
      setForm(emptyForm);
    }
    setFormError(null);
    setModalMode('edit');
  }, []);

  const handleSaveEdit = useCallback(() => {
    if (!form.titulo.trim()) {
      setFormError('Título é obrigatório.');
      return;
    }
    setFormError(null);
    const body: ServicoRequest = {
      titulo: form.titulo,
      descricao: form.descricao || undefined,
      validoDe: form.validoDe || undefined,
      validoAte: form.validoAte || undefined,
      ativo: form.ativo,
      duracaoMinutos: form.duracaoMinutos,
    };
    if (selectedId != null) {
      updateMutation.mutate({ id: selectedId, body });
    } else {
      createMutation.mutate(body);
    }
  }, [form, selectedId, updateMutation, createMutation]);

  const handleDelete = useCallback((row: ServicoResponse) => {
    if (!window.confirm(`Excluir o serviço "${row.titulo}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const columns = [
    { name: 'Título', selector: (r: ServicoResponse) => r.titulo, sortable: true },
    { name: 'Duração (min)', cell: (r: ServicoResponse) => r.duracaoMinutos ?? '—', sortable: true },
    { name: 'Ativo', cell: (r: ServicoResponse) => (r.ativo ? 'Sim' : 'Não'), sortable: true },
    { name: 'Válido de', cell: (r: ServicoResponse) => formatLocalDate(r.validoDe), sortable: false },
    { name: 'Válido até', cell: (r: ServicoResponse) => formatLocalDate(r.validoAte), sortable: false },
    {
      name: 'Ações',
      cell: (row: ServicoResponse) => (
        <div className="flex gap-1">
          <button type="button" onClick={() => openDetail(row)} className="rounded p-1.5 bg-zinc-600 text-white hover:bg-zinc-500" title="Ver" aria-label="Ver"><Eye className="size-4" /></button>
          <button type="button" onClick={() => openEdit(row)} className="rounded p-1.5 bg-zinc-600 text-white hover:bg-zinc-500" title="Editar" aria-label="Editar"><Pencil className="size-4" /></button>
          <button type="button" onClick={() => handleDelete(row)} className="rounded p-1.5 bg-red-800 text-white hover:bg-red-700" title="Excluir" aria-label="Excluir"><Trash2 className="size-4" /></button>
        </div>
      ),
      ignoreRowClick: true,
      allowOverflow: true,
      button: true,
    },
  ];

  const detailData = modalMode === 'detail' && selected ? selected : null;
  const editOpen = modalMode === 'edit';

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Serviços</h1>
        <button type="button" onClick={() => openEdit(null)} className="flex items-center gap-2 rounded bg-zinc-600 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-500"><Plus className="size-4" /> Novo serviço</button>
      </div>
      {isError && <p className="mb-4 text-red-400" role="alert">Erro ao carregar serviços.</p>}
      <DataTable columns={columns} data={list} progressPending={isLoading} customStyles={tableStyles} pagination paginationPerPage={10} noDataComponent="Nenhum serviço." />

      <Modal open={!!detailData} onClose={() => { setModalMode(null); setSelectedId(null); }} title="Detalhe do serviço">
        {detailData && (
          <dl className="space-y-2">
            <div><dt className="text-sm text-zinc-500">Título</dt><dd className="text-white">{detailData.titulo}</dd></div>
            <div><dt className="text-sm text-zinc-500">Descrição</dt><dd className="text-white">{detailData.descricao ?? '—'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Duração (min)</dt><dd className="text-white">{detailData.duracaoMinutos ?? '—'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Válido de</dt><dd className="text-white">{formatLocalDate(detailData.validoDe)}</dd></div>
            <div><dt className="text-sm text-zinc-500">Válido até</dt><dd className="text-white">{formatLocalDate(detailData.validoAte)}</dd></div>
            <div><dt className="text-sm text-zinc-500">Ativo</dt><dd className="text-white">{detailData.ativo ? 'Sim' : 'Não'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Criado em</dt><dd className="text-white">{formatDate(detailData.createdAt)}</dd></div>
          </dl>
        )}
      </Modal>

      <Modal open={editOpen} onClose={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); setFormError(null); }} title={selectedId != null ? 'Editar serviço' : 'Novo serviço'}>
        <div className="space-y-4">
          {formError && <p className="text-sm text-red-400" role="alert">{formError}</p>}
          <div>
            <label htmlFor="servico-titulo" className="mb-1 block text-sm text-zinc-400">Título</label>
            <input id="servico-titulo" type="text" value={form.titulo} onChange={(e) => setForm((f) => ({ ...f, titulo: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div>
            <label htmlFor="servico-descricao" className="mb-1 block text-sm text-zinc-400">Descrição</label>
            <textarea id="servico-descricao" value={form.descricao ?? ''} onChange={(e) => setForm((f) => ({ ...f, descricao: e.target.value }))} rows={3} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="servico-validoDe" className="mb-1 block text-sm text-zinc-400">Válido de</label>
              <input id="servico-validoDe" type="date" value={form.validoDe ?? ''} onChange={(e) => setForm((f) => ({ ...f, validoDe: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
            </div>
            <div>
              <label htmlFor="servico-validoAte" className="mb-1 block text-sm text-zinc-400">Válido até</label>
              <input id="servico-validoAte" type="date" value={form.validoAte ?? ''} onChange={(e) => setForm((f) => ({ ...f, validoAte: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
            </div>
          </div>
          <div>
            <label htmlFor="servico-duracao" className="mb-1 block text-sm text-zinc-400">Duração (minutos)</label>
            <input id="servico-duracao" type="number" min={1} value={form.duracaoMinutos ?? 30} onChange={(e) => setForm((f) => ({ ...f, duracaoMinutos: parseInt(e.target.value, 10) || 30 }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div className="flex items-center gap-2">
            <input id="servico-ativo" type="checkbox" checked={form.ativo ?? true} onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))} className="rounded border-zinc-600" />
            <label htmlFor="servico-ativo" className="text-sm text-zinc-400">Ativo</label>
          </div>
          <div className="flex justify-end gap-2">
            <button type="button" onClick={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); }} className="rounded bg-zinc-700 px-4 py-2 text-sm text-white hover:bg-zinc-600">Cancelar</button>
            <button type="button" onClick={handleSaveEdit} disabled={createMutation.isPending || updateMutation.isPending} className="rounded bg-zinc-600 px-4 py-2 text-sm text-white hover:bg-zinc-500 disabled:opacity-50">{selectedId != null ? 'Salvar' : 'Criar'}</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
