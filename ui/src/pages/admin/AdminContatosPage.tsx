import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchContatos,
  fetchContatoById,
  createContato,
  updateContato,
  deleteContato,
  type ContatoResponse,
  type ContatoRequest,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react';

const emptyForm: ContatoRequest = { nome: '', email: '', telefone: '', mensagem: '' };

function formatDate(iso: string) {
  try {
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

const tableStyles = {
  table: {
    style: { backgroundColor: 'rgb(24 24 27)' },
  },
  headRow: {
    style: { backgroundColor: 'rgb(39 39 42)', minHeight: '40px' },
  },
  headCells: {
    style: { color: 'rgb(228 228 231)', fontSize: '0.875rem' },
  },
  rows: {
    style: { color: 'rgb(228 228 231)', backgroundColor: 'rgb(24 24 27)' },
  },
  pagination: {
    style: { backgroundColor: 'rgb(24 24 27)', color: 'rgb(228 228 231)' },
  },
};

export function AdminContatosPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<ContatoRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'contatos'],
    queryFn: () => fetchContatos(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'contatos', selectedId],
    queryFn: () => fetchContatoById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createContato,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'contatos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: ContatoRequest }) => updateContato(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'contatos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'contatos', selectedId] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteContato,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'contatos'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard', 'summary'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: ContatoResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: ContatoResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({
        nome: row.nome,
        email: row.email,
        telefone: row.telefone ?? '',
        mensagem: row.mensagem ?? '',
      });
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

  const handleDelete = useCallback((row: ContatoResponse) => {
    if (!window.confirm(`Excluir o contato "${row.nome}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const columns = [
    { name: 'Nome', selector: (r: ContatoResponse) => r.nome, sortable: true },
    { name: 'E-mail', selector: (r: ContatoResponse) => r.email, sortable: true },
    { name: 'Telefone', selector: (r: ContatoResponse) => r.telefone ?? '—', sortable: false },
    {
      name: 'Ações',
      cell: (row: ContatoResponse) => (
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
        <h1 className="text-2xl font-bold text-white">Contatos</h1>
        <button type="button" onClick={() => openEdit(null)} className="flex items-center gap-2 rounded bg-zinc-600 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-500"><Plus className="size-4" /> Novo contato</button>
      </div>

      {isError && (
        <p className="mb-4 text-red-400" role="alert">
          Erro ao carregar contatos.
        </p>
      )}

      <DataTable
        columns={columns}
        data={list}
        progressPending={isLoading}
        customStyles={tableStyles}
        pagination
        paginationPerPage={10}
        noDataComponent="Nenhum contato."
      />

      <Modal
        open={!!detailData}
        onClose={() => { setModalMode(null); setSelectedId(null); }}
        title="Detalhe do contato"
      >
        {detailData && (
          <dl className="space-y-2">
            <div>
              <dt className="text-sm text-zinc-500">Nome</dt>
              <dd className="text-white">{detailData.nome}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">E-mail</dt>
              <dd className="text-white">{detailData.email}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Telefone</dt>
              <dd className="text-white">{detailData.telefone ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Mensagem</dt>
              <dd className="text-white">{detailData.mensagem ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-sm text-zinc-500">Criado em</dt>
              <dd className="text-white">{formatDate(detailData.createdAt)}</dd>
            </div>
          </dl>
        )}
      </Modal>

      <Modal
        open={editOpen}
        onClose={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); setFormError(null); }}
        title={selectedId != null ? 'Editar contato' : 'Novo contato'}
      >
        <div className="space-y-4">
          {formError && (
            <p className="text-sm text-red-400" role="alert">{formError}</p>
          )}
          <div>
            <label htmlFor="contato-nome" className="mb-1 block text-sm text-zinc-400">Nome</label>
            <input
              id="contato-nome"
              type="text"
              value={form.nome}
              onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))}
              className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white"
            />
          </div>
          <div>
            <label htmlFor="contato-email" className="mb-1 block text-sm text-zinc-400">E-mail</label>
            <input
              id="contato-email"
              type="email"
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white"
            />
          </div>
          <div>
            <label htmlFor="contato-telefone" className="mb-1 block text-sm text-zinc-400">Telefone</label>
            <input
              id="contato-telefone"
              type="text"
              value={form.telefone ?? ''}
              onChange={(e) => setForm((f) => ({ ...f, telefone: e.target.value }))}
              className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white"
            />
          </div>
          <div>
            <label htmlFor="contato-mensagem" className="mb-1 block text-sm text-zinc-400">Mensagem</label>
            <textarea
              id="contato-mensagem"
              value={form.mensagem ?? ''}
              onChange={(e) => setForm((f) => ({ ...f, mensagem: e.target.value }))}
              rows={3}
              className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white"
            />
          </div>
          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); }}
              className="rounded bg-zinc-700 px-4 py-2 text-sm text-white hover:bg-zinc-600"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveEdit}
              disabled={createMutation.isPending || updateMutation.isPending}
              className="rounded bg-zinc-600 px-4 py-2 text-sm text-white hover:bg-zinc-500 disabled:opacity-50"
            >
              {selectedId != null ? 'Salvar' : 'Criar'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
