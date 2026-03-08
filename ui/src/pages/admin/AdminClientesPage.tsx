import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchClientes,
  fetchClienteById,
  createCliente,
  updateCliente,
  deleteCliente,
  type ClienteResponse,
  type ClienteRequest,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';

const emptyForm: ClienteRequest = { nome: '', email: '', telefone: '' };

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
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

export function AdminClientesPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<ClienteRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'clientes'],
    queryFn: () => fetchClientes(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'clientes', selectedId],
    queryFn: () => fetchClienteById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createCliente,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'clientes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: ClienteRequest }) => updateCliente(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'clientes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'clientes', selectedId] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteCliente,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'clientes'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: ClienteResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: ClienteResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({ nome: row.nome, email: row.email, telefone: row.telefone ?? '' });
    } else {
      setSelectedId(null);
      setForm(emptyForm);
    }
    setFormError(null);
    setModalMode('edit');
  }, []);

  const handleSaveEdit = useCallback(() => {
    if (!form.nome.trim() || !form.email.trim()) {
      setFormError('Nome e e-mail são obrigatórios.');
      return;
    }
    setFormError(null);
    if (selectedId != null) {
      updateMutation.mutate({ id: selectedId, body: form });
    } else {
      createMutation.mutate(form);
    }
  }, [form, selectedId, updateMutation, createMutation]);

  const handleDelete = useCallback((row: ClienteResponse) => {
    if (!window.confirm(`Excluir o cliente "${row.nome}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const columns = [
    { name: 'Nome', selector: (r: ClienteResponse) => r.nome, sortable: true },
    { name: 'E-mail', selector: (r: ClienteResponse) => r.email, sortable: true },
    { name: 'Telefone', selector: (r: ClienteResponse) => r.telefone ?? '—', sortable: false },
    {
      name: 'Ações',
      cell: (row: ClienteResponse) => (
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
        <h1 className="text-2xl font-bold text-white">Clientes</h1>
        <button type="button" onClick={() => openEdit(null)} className="rounded bg-zinc-600 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-500">Novo cliente</button>
      </div>
      {isError && <p className="mb-4 text-red-400" role="alert">Erro ao carregar clientes.</p>}
      <DataTable columns={columns} data={list} progressPending={isLoading} customStyles={tableStyles} pagination paginationPerPage={10} noDataComponent="Nenhum cliente." />

      <Modal open={!!detailData} onClose={() => { setModalMode(null); setSelectedId(null); }} title="Detalhe do cliente">
        {detailData && (
          <dl className="space-y-2">
            <div><dt className="text-sm text-zinc-500">Nome</dt><dd className="text-white">{detailData.nome}</dd></div>
            <div><dt className="text-sm text-zinc-500">E-mail</dt><dd className="text-white">{detailData.email}</dd></div>
            <div><dt className="text-sm text-zinc-500">Telefone</dt><dd className="text-white">{detailData.telefone ?? '—'}</dd></div>
            <div><dt className="text-sm text-zinc-500">Criado em</dt><dd className="text-white">{formatDate(detailData.createdAt)}</dd></div>
          </dl>
        )}
      </Modal>

      <Modal open={editOpen} onClose={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); setFormError(null); }} title={selectedId != null ? 'Editar cliente' : 'Novo cliente'}>
        <div className="space-y-4">
          {formError && <p className="text-sm text-red-400" role="alert">{formError}</p>}
          <div>
            <label htmlFor="cliente-nome" className="mb-1 block text-sm text-zinc-400">Nome</label>
            <input id="cliente-nome" type="text" value={form.nome} onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div>
            <label htmlFor="cliente-email" className="mb-1 block text-sm text-zinc-400">E-mail</label>
            <input id="cliente-email" type="email" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div>
            <label htmlFor="cliente-telefone" className="mb-1 block text-sm text-zinc-400">Telefone</label>
            <input id="cliente-telefone" type="text" value={form.telefone ?? ''} onChange={(e) => setForm((f) => ({ ...f, telefone: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
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
