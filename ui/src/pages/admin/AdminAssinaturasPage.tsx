import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchAssinaturas,
  fetchAssinaturaById,
  fetchClientes,
  fetchServicos,
  createAssinatura,
  updateAssinatura,
  deleteAssinatura,
  type AssinaturaResponse,
  type AssinaturaRequest,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react';

const emptyForm: AssinaturaRequest = { clienteId: 0, servicoIds: [] };

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

export function AdminAssinaturasPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<AssinaturaRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'assinaturas'],
    queryFn: () => fetchAssinaturas(),
  });

  const { data: clientes = [] } = useQuery({
    queryKey: ['admin', 'clientes'],
    queryFn: () => fetchClientes(),
  });

  const { data: servicos = [] } = useQuery({
    queryKey: ['admin', 'servicos'],
    queryFn: () => fetchServicos(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'assinaturas', selectedId],
    queryFn: () => fetchAssinaturaById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createAssinatura,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'assinaturas'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: AssinaturaRequest }) => updateAssinatura(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'assinaturas'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'assinaturas', selectedId] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAssinatura,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'assinaturas'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: AssinaturaResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: AssinaturaResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({
        clienteId: row.clienteId,
        servicoIds: row.servicos.map((s) => s.id),
      });
    } else {
      setSelectedId(null);
      setForm(emptyForm);
    }
    setFormError(null);
    setModalMode('edit');
  }, []);

  const handleSaveEdit = useCallback(() => {
    if (!form.clienteId) {
      setFormError('Selecione o cliente.');
      return;
    }
    setFormError(null);
    if (selectedId != null) {
      updateMutation.mutate({ id: selectedId, body: form });
    } else {
      createMutation.mutate(form);
    }
  }, [form, selectedId, updateMutation, createMutation]);

  const handleDelete = useCallback((row: AssinaturaResponse) => {
    if (!window.confirm(`Excluir a assinatura do cliente "${row.clienteNome}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const toggleServico = useCallback((servicoId: number) => {
    setForm((prev) => {
      const ids = prev.servicoIds.includes(servicoId)
        ? prev.servicoIds.filter((id) => id !== servicoId)
        : [...prev.servicoIds, servicoId];
      return { ...prev, servicoIds: ids };
    });
  }, []);

  const columns = [
    { name: 'Cliente', selector: (r: AssinaturaResponse) => r.clienteNome, sortable: true },
    {
      name: 'Serviços',
      cell: (r: AssinaturaResponse) => r.servicos.map((s) => s.titulo).join(', ') || '—',
      sortable: false,
    },
    {
      name: 'Ações',
      cell: (r: AssinaturaResponse) => (
        <div className="flex gap-1">
          <button type="button" onClick={() => openDetail(r)} className="rounded p-1.5 text-amber-500 hover:bg-amber-500/20" title="Ver" aria-label="Ver"><Eye className="size-4" /></button>
          <button type="button" onClick={() => openEdit(r)} className="rounded p-1.5 text-amber-500 hover:bg-amber-500/20" title="Editar" aria-label="Editar"><Pencil className="size-4" /></button>
          <button type="button" onClick={() => handleDelete(r)} className="rounded p-1.5 text-red-400 hover:bg-red-400/20" title="Excluir" aria-label="Excluir"><Trash2 className="size-4" /></button>
        </div>
      ),
      width: '180px',
    },
  ];

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-white">Assinaturas</h1>
        <button type="button" onClick={() => openEdit(null)} className="flex items-center gap-2 rounded-md bg-amber-500 px-4 py-2 text-sm font-medium text-zinc-950 hover:bg-amber-400"><Plus className="size-4" /> Nova assinatura</button>
      </div>
      {isError && <p className="mb-4 text-red-400">Erro ao carregar assinaturas.</p>}
      <DataTable
        columns={columns}
        data={list}
        progressPending={isLoading}
        customStyles={tableStyles}
        pagination
        noDataComponent="Nenhuma assinatura cadastrada."
      />

      <Modal
        open={modalMode === 'detail' && selected != null}
        onClose={() => setModalMode(null)}
        title="Assinatura"
      >
        {selected && (
          <div className="space-y-3">
            <p><span className="text-zinc-500">Cliente:</span> {selected.clienteNome}</p>
            <p><span className="text-zinc-500">Serviços:</span> {selected.servicos.map((s) => s.titulo).join(', ') || '—'}</p>
            <p><span className="text-zinc-500">Criado em:</span> {formatDate(selected.createdAt)}</p>
          </div>
        )}
      </Modal>

      <Modal
        open={modalMode === 'edit'}
        onClose={() => setModalMode(null)}
        title={selectedId != null ? 'Editar assinatura' : 'Nova assinatura'}
      >
        <div className="space-y-4">
          {formError && <p className="text-red-400">{formError}</p>}
          <div>
            <label htmlFor="edit-cliente" className="mb-1 block text-sm text-zinc-400">Cliente</label>
            <select
              id="edit-cliente"
              value={form.clienteId || ''}
              onChange={(e) => setForm((prev) => ({ ...prev, clienteId: Number(e.target.value) }))}
              className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white"
            >
              <option value="">Selecione</option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id}>{c.nome} – {c.email}</option>
              ))}
            </select>
          </div>
          <div>
            <span className="mb-2 block text-sm text-zinc-400">Serviços</span>
            <div className="max-h-40 space-y-2 overflow-y-auto rounded border border-zinc-600 bg-zinc-800 p-2">
              {servicos.map((s) => (
                <label key={s.id} className="flex cursor-pointer items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={form.servicoIds.includes(s.id)}
                    onChange={() => toggleServico(s.id)}
                    className="rounded text-amber-500"
                  />
                  {s.titulo}
                </label>
              ))}
            </div>
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={handleSaveEdit}
              disabled={createMutation.isPending || updateMutation.isPending}
              className="rounded bg-amber-500 px-4 py-2 text-sm font-medium text-zinc-950 hover:bg-amber-400 disabled:opacity-50"
            >
              {selectedId != null ? 'Salvar' : 'Criar'}
            </button>
            <button
              type="button"
              onClick={() => setModalMode(null)}
              className="rounded border border-zinc-600 px-4 py-2 text-sm text-zinc-300 hover:bg-zinc-800"
            >
              Cancelar
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
