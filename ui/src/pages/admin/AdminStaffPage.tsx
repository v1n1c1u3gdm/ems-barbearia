import { useState, useCallback } from 'react';
import DataTable from 'react-data-table-component';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchStaff,
  fetchStaffById,
  createStaff,
  updateStaff,
  deleteStaff,
  type StaffResponse,
  type StaffRequest,
  type HorarioFuncionamentoStaff,
} from '@/features/admin/api';
import { Modal } from '@/components/ui/Modal';
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react';

const DIAS = ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

function defaultHorarios(): StaffRequest['horarios'] {
  return [
    { diaSemana: 0, aberto: false, horaInicio: null, horaFim: null },
    { diaSemana: 1, aberto: false, horaInicio: null, horaFim: null },
    { diaSemana: 2, aberto: true, horaInicio: '09:00', horaFim: '19:00' },
    { diaSemana: 3, aberto: true, horaInicio: '09:00', horaFim: '19:00' },
    { diaSemana: 4, aberto: true, horaInicio: '09:00', horaFim: '19:00' },
    { diaSemana: 5, aberto: true, horaInicio: '09:00', horaFim: '19:00' },
    { diaSemana: 6, aberto: true, horaInicio: '09:00', horaFim: '16:00' },
  ];
}

function buildHorariosFromResponse(horarios: HorarioFuncionamentoStaff[] | undefined): StaffRequest['horarios'] {
  const def = defaultHorarios()!;
  if (!horarios?.length) return def;
  const byDay = new Map(horarios.map((h) => [h.diaSemana, h]));
  return def.map((d) => {
    const from = byDay.get(d.diaSemana);
    if (!from) return d;
    const hi = from.horaInicio ? from.horaInicio.slice(0, 5) : null;
    const hf = from.horaFim ? from.horaFim.slice(0, 5) : null;
    return { diaSemana: d.diaSemana, aberto: from.aberto, horaInicio: hi, horaFim: hf };
  });
}

function formatDisponibilidadeSummary(horarios: HorarioFuncionamentoStaff[] | undefined): string {
  if (!horarios?.length) return '—';
  const parts: string[] = [];
  for (const h of horarios) {
    if (!h.aberto || h.horaInicio == null || h.horaFim == null) continue;
    const hi = h.horaInicio.slice(0, 5);
    const hf = h.horaFim.slice(0, 5);
    parts.push(`${DIAS[h.diaSemana].slice(0, 3)} ${hi}–${hf}`);
  }
  return parts.length === 0 ? 'Fechado' : parts.join('; ');
}

const emptyForm: StaffRequest = { nome: '', ativo: true, horarios: defaultHorarios() };

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

export function AdminStaffPage() {
  const queryClient = useQueryClient();
  const [modalMode, setModalMode] = useState<'detail' | 'edit' | null>(null);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<StaffRequest>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);

  const { data: list = [], isLoading, isError } = useQuery({
    queryKey: ['admin', 'staff'],
    queryFn: () => fetchStaff(),
  });

  const { data: selected } = useQuery({
    queryKey: ['admin', 'staff', selectedId],
    queryFn: () => fetchStaffById(selectedId!),
    enabled: selectedId != null && (modalMode === 'detail' || modalMode === 'edit'),
  });

  const createMutation = useMutation({
    mutationFn: createStaff,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'staff'] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: StaffRequest }) => updateStaff(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'staff'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'staff', selectedId] });
      setModalMode(null);
      setSelectedId(null);
      setForm(emptyForm);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteStaff,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'staff'] });
      setModalMode(null);
      setSelectedId(null);
    },
  });

  const openDetail = useCallback((row: StaffResponse) => {
    setSelectedId(row.id);
    setModalMode('detail');
  }, []);

  const openEdit = useCallback((row: StaffResponse | null) => {
    if (row) {
      setSelectedId(row.id);
      setForm({ nome: row.nome, ativo: row.ativo, horarios: buildHorariosFromResponse(row.horarios) });
    } else {
      setSelectedId(null);
      setForm(emptyForm);
    }
    setFormError(null);
    setModalMode('edit');
  }, []);

  const handleSaveEdit = useCallback(() => {
    if (!form.nome.trim()) {
      setFormError('Nome é obrigatório.');
      return;
    }
    setFormError(null);
    if (selectedId != null) {
      updateMutation.mutate({ id: selectedId, body: form });
    } else {
      createMutation.mutate(form);
    }
  }, [form, selectedId, updateMutation, createMutation]);

  const handleDelete = useCallback((row: StaffResponse) => {
    if (!window.confirm(`Excluir o staff "${row.nome}"?`)) return;
    deleteMutation.mutate(row.id);
  }, [deleteMutation]);

  const columns = [
    { name: 'Nome', selector: (r: StaffResponse) => r.nome, sortable: true },
    { name: 'Ativo', cell: (r: StaffResponse) => (r.ativo ? 'Sim' : 'Não'), sortable: true },
    { name: 'Disponibilidade', cell: (r: StaffResponse) => formatDisponibilidadeSummary(r.horarios), sortable: false, wrap: true },
    {
      name: 'Ações',
      cell: (row: StaffResponse) => (
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
        <h1 className="text-2xl font-bold text-white">Staff</h1>
        <button type="button" onClick={() => openEdit(null)} className="flex items-center gap-2 rounded bg-zinc-600 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-500"><Plus className="size-4" /> Novo staff</button>
      </div>
      {isError && <p className="mb-4 text-red-400" role="alert">Erro ao carregar staff.</p>}
      <DataTable columns={columns} data={list} progressPending={isLoading} customStyles={tableStyles} pagination paginationPerPage={10} noDataComponent="Nenhum staff." />

      <Modal open={!!detailData} onClose={() => { setModalMode(null); setSelectedId(null); }} title="Detalhe do staff">
        {detailData && (
          <>
            <dl className="space-y-2">
              <div><dt className="text-sm text-zinc-500">Nome</dt><dd className="text-white">{detailData.nome}</dd></div>
              <div><dt className="text-sm text-zinc-500">Ativo</dt><dd className="text-white">{detailData.ativo ? 'Sim' : 'Não'}</dd></div>
              <div><dt className="text-sm text-zinc-500">Criado em</dt><dd className="text-white">{formatDate(detailData.createdAt)}</dd></div>
            </dl>
            <div className="mt-4">
              <h3 className="mb-2 text-sm font-medium text-zinc-400">Disponibilidade</h3>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-zinc-500">
                    <th className="pb-1 pr-2">Dia</th>
                    <th className="pb-1 pr-2">Aberto</th>
                    <th className="pb-1">Início – Fim</th>
                  </tr>
                </thead>
                <tbody className="text-white">
                  {(detailData.horarios ?? []).length === 0
                    ? <tr><td colSpan={3} className="py-1 text-zinc-500">Nenhum horário cadastrado.</td></tr>
                    : (buildHorariosFromResponse(detailData.horarios) ?? []).map((h) => (
                        <tr key={h.diaSemana}>
                          <td className="py-0.5 pr-2">{DIAS[h.diaSemana]}</td>
                          <td className="py-0.5 pr-2">{h.aberto ? 'Sim' : 'Não'}</td>
                          <td className="py-0.5">{h.aberto && h.horaInicio && h.horaFim ? `${h.horaInicio.slice(0, 5)} – ${h.horaFim.slice(0, 5)}` : '—'}</td>
                        </tr>
                      ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </Modal>

      <Modal open={editOpen} onClose={() => { setModalMode(null); setSelectedId(null); setForm(emptyForm); setFormError(null); }} title={selectedId != null ? 'Editar staff' : 'Novo staff'}>
        <div className="space-y-4">
          {formError && <p className="text-sm text-red-400" role="alert">{formError}</p>}
          <div>
            <label htmlFor="staff-nome" className="mb-1 block text-sm text-zinc-400">Nome</label>
            <input id="staff-nome" type="text" value={form.nome} onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))} className="w-full rounded border border-zinc-600 bg-zinc-800 px-3 py-2 text-white" />
          </div>
          <div className="flex items-center gap-2">
            <input id="staff-ativo" type="checkbox" checked={form.ativo ?? true} onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))} className="rounded border-zinc-600" />
            <label htmlFor="staff-ativo" className="text-sm text-zinc-400">Ativo</label>
          </div>
          <div>
            <h3 className="mb-2 text-sm font-medium text-zinc-400">Disponibilidade</h3>
            <div className="space-y-2">
              {(form.horarios ?? defaultHorarios() ?? []).map((h, idx) => (
                <div key={h.diaSemana} className="flex flex-wrap items-center gap-2 rounded border border-zinc-700 bg-zinc-800/50 px-3 py-2">
                  <span className="w-24 text-sm text-zinc-300">{DIAS[h.diaSemana]}</span>
                  <label className="flex items-center gap-1 text-sm text-zinc-400">
                    <input
                      type="checkbox"
                      checked={h.aberto}
                      onChange={(e) => {
                        const next = [...(form.horarios ?? [])];
                        next[idx] = { ...next[idx], aberto: e.target.checked, horaInicio: next[idx].horaInicio ?? '09:00', horaFim: next[idx].horaFim ?? '18:00' };
                        setForm((f) => ({ ...f, horarios: next }));
                      }}
                      className="rounded border-zinc-600"
                    />
                    Aberto
                  </label>
                  {h.aberto && (
                    <>
                      <input
                        type="time"
                        aria-label={`Início ${DIAS[h.diaSemana]}`}
                        value={h.horaInicio ?? '09:00'}
                        onChange={(e) => {
                          const next = [...(form.horarios ?? [])];
                          next[idx] = { ...next[idx], horaInicio: e.target.value || null };
                          setForm((f) => ({ ...f, horarios: next }));
                        }}
                        className="rounded border border-zinc-600 bg-zinc-800 px-2 py-1 text-sm text-white"
                      />
                      <span className="text-zinc-500">–</span>
                      <input
                        type="time"
                        aria-label={`Fim ${DIAS[h.diaSemana]}`}
                        value={h.horaFim ?? '18:00'}
                        onChange={(e) => {
                          const next = [...(form.horarios ?? [])];
                          next[idx] = { ...next[idx], horaFim: e.target.value || null };
                          setForm((f) => ({ ...f, horarios: next }));
                        }}
                        className="rounded border border-zinc-600 bg-zinc-800 px-2 py-1 text-sm text-white"
                      />
                    </>
                  )}
                </div>
              ))}
            </div>
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
