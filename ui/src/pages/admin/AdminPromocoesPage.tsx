import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchPromocoes,
  fetchPromocaoById,
  createPromocao,
  updatePromocao,
  deletePromocao,
  type PromocaoResponse,
  type PromocaoRequest,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';

const emptyForm: PromocaoRequest = {
  titulo: '',
  descricao: '',
  validoDe: '',
  validoAte: '',
  ativo: true,
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

export function AdminPromocoesPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<PromocaoRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'promocoes'],
    queryFn: () => fetchPromocoes(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'promocoes', selectedId],
    queryFn: () => fetchPromocaoById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createPromocao,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'promocoes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: PromocaoRequest }) => updatePromocao(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'promocoes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'promocoes', selectedId] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deletePromocao,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'promocoes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: PromocaoResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: PromocaoResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({
        titulo: row.titulo,
        descricao: row.descricao ?? '',
        validoDe: row.validoDe ?? '',
        validoAte: row.validoAte ?? '',
        ativo: row.ativo,
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
    const body: PromocaoRequest = {
      titulo: form.titulo,
      descricao: form.descricao || undefined,
      validoDe: form.validoDe || undefined,
      validoAte: form.validoAte || undefined,
      ativo: form.ativo,
    };
    if (selectedId != null) {
      updateMutation.mutate({ id: selectedId, body });
    } else {
      createMutation.mutate(body);
    }
  }, [form, selectedId, updateMutation, createMutation]);

  const handleDelete = useCallback((row: PromocaoResponse) => {
    if (!window.confirm(`Excluir a promoção "${row.titulo}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const columns = [
    { name: 'Título', selector: (r: PromocaoResponse) => r.titulo, sortable: true },
    { name: 'Ativo', cell: (r: PromocaoResponse) => (r.ativo ? 'Sim' : 'Não'), sortable: true },
    { name: 'Válido de', cell: (r: PromocaoResponse) => formatLocalDate(r.validoDe), sortable: false },
    { name: 'Válido até', cell: (r: PromocaoResponse) => formatLocalDate(r.validoAte), sortable: false },
    {
      name: 'Ações',
      cell: (row: PromocaoResponse) => (
        <div className="flex gap-2">
          <button type="button" onClick={() => openDetail(row)} className="rounded bg-zinc-600 px-2 py-1 text-xs text-white hover:bg-zinc-500">Ver</button>
          <button type="button" onClick={() => openEdit(row)} className="rounded bg-zinc-600 px-2 py-1 text-xs text-white hover:bg-zinc-500">Editar</button>
          <button type="button" onClick={() => handleDelete(row)} className="rounded bg-red-800 px-2 py-1 text-xs text-white hover:bg-red-700">Excluir</button>
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
        <h1 className="text-2xl font-bold text-white">Promoções</h1>
        <button type="button" onClick={() => openEdit(null)} className="rounded bg-zinc-600 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-500">Nova promoção</button>
      </div>
      {isError && <p className="mb-4 text-red-400" role="alert">Erro ao carregar promoções.</p>}
      <DataTable columns={columns} data={list} progressPending={isLoading} customStyles={tableStyles} pagination paginationPerPage={10} noDataComponent="Nenhuma promoção." />

      <Modal open={!!detailData} onClose={() => { setModalMode(null); setSelectedId(null); }} title="Detalhe da promoção">
        {detailData && (
          <dl className="space-y-2">
            <div><dt className="text-sm text-zinc-500">Título</dt><dd className="text-white">{detailData.titulo}</dd></div>
            <div><dt className="text-sm text-zinc-500">Descrição</dt><dd className="text-white">{detailData.descricao ?? '—'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Válido de</dt><dd className="text-white">{formatLocalDate(detailData.validoDe)}</dd></div>
            <div><dt className="text-sm text-zinc-500">Válido até</dt><dd className="text-white">{formatLocalDate(detailData.validoAte)}</dd></div>
            <div><dt className="text-sm text-zinc-500">Ativo</dt><dd className="text-white">{detailData.ativo ? 'Sim' : 'Não'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Criado em</dt><dd className="text-white">{formatDate(detailData.createdAt)}</dd></div>
          </dl>
        )}
      </Modal>

      <Modal open={editOpen} onClose={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); setFormError(null); }} title={selectedId != null ? 'Editar promoção' : 'Nova promoção'}>
        <div className="space-y-4">
          {formError && <p className="text-sm text-red-400" role="alert">{formError}</p>}
          <div>
            <label htmlFor="promo-titulo" className="mb-1 block text-sm text-zinc-400">Título</label>
            <input id="promo-titulo" type="text" value={form.titulo} onChange={(e) => setForm((f) => ({ ...f, titulo: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div>
            <label htmlFor="promo-descricao" className="mb-1 block text-sm text-zinc-400">Descrição</label>
            <textarea id="promo-descricao" value={form.descricao ?? ''} onChange={(e) => setForm((f) => ({ ...f, descricao: e.target.value }))} rows={3} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="promo-validoDe" className="mb-1 block text-sm text-zinc-400">Válido de</label>
              <input id="promo-validoDe" type="date" value={form.validoDe ?? ''} onChange={(e) => setForm((f) => ({ ...f, validoDe: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
            </div>
            <div>
              <label htmlFor="promo-validoAte" className="mb-1 block text-sm text-zinc-400">Válido até</label>
              <input id="promo-validoAte" type="date" value={form.validoAte ?? ''} onChange={(e) => setForm((f) => ({ ...f, validoAte: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
            </div>
          </div>
          <div className="flex items-center gap-2">
            <input id="promo-ativo" type="checkbox" checked={form.ativo ?? true} onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))} className="rounded border-zinc-600" />
            <label htmlFor="promo-ativo" className="text-sm text-zinc-400">Ativo</label>
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
