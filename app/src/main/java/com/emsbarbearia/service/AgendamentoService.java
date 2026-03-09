package com.emsbarbearia.service;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.dto.PublicSlotResponse;
import com.emsbarbearia.entity.Agendamento;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.entity.Staff;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ServicoRepository;
import com.emsbarbearia.repository.StaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AgendamentoService {

    private static final String TIPO_FIRME = "FIRME";
    private static final String TIPO_ENCAIXE = "ENCAIXE";
    private static final String STATUS_PENDENTE = "PENDENTE";

    private final AgendamentoRepository repository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;

    public AgendamentoService(AgendamentoRepository repository, ClienteRepository clienteRepository,
                             ServicoRepository servicoRepository, StaffRepository staffRepository,
                             AuditLogService auditLogService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.staffRepository = staffRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> list(Long clienteId, Long staffId, String status, Instant de, Instant ate) {
        Stream<Agendamento> stream;
        if (de != null && ate != null) {
            stream = repository.findByDataHoraBetweenOrderByDataHora(de, ate).stream();
        } else if (clienteId != null) {
            stream = repository.findByClienteId(clienteId).stream();
        } else if (staffId != null) {
            stream = repository.findByStaffId(staffId).stream();
        } else {
            stream = repository.findAll().stream();
        }
        if (clienteId != null) stream = stream.filter(a -> a.getCliente().getId().equals(clienteId));
        if (staffId != null) stream = stream.filter(a -> a.getStaff() != null && a.getStaff().getId().equals(staffId));
        if (status != null && !status.isBlank()) stream = stream.filter(a -> status.equals(a.getStatus()));
        return stream.map(a -> toResponse(a, null)).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicSlotResponse> listPublicSlots(Instant de, Instant ate, Long staffId) {
        if (de == null || ate == null) return List.of();
        List<Agendamento> list = repository.findByDataHoraBetweenOrderByDataHora(de, ate);
        Stream<Agendamento> stream = list.stream().filter(a -> !"CANCELADO".equals(a.getStatus()));
        if (staffId != null) stream = stream.filter(a -> a.getStaff() != null && a.getStaff().getId().equals(staffId));
        return stream.sorted((a, b) -> {
            int c = a.getDataHora().compareTo(b.getDataHora());
            return c != 0 ? c : (a.getCreatedAt() != null && b.getCreatedAt() != null ? a.getCreatedAt().compareTo(b.getCreatedAt()) : 0);
        }).map(this::toPublicSlotResponse).toList();
    }

    private PublicSlotResponse toPublicSlotResponse(Agendamento entity) {
        Staff st = entity.getStaff();
        return new PublicSlotResponse(
            st != null ? st.getId() : null,
            st != null ? st.getNome() : null,
            entity.getDataHora(),
            entity.getDataHoraFim(),
            entity.getTipo(),
            entity.getStatus()
        );
    }

    public Optional<AgendamentoResponse> getById(Long id) {
        return repository.findById(id).map(e -> toResponse(e, null));
    }

    public Optional<AgendamentoResponse> create(AgendamentoRequest request) {
        String status = request.status() != null && !request.status().isBlank() ? request.status() : STATUS_PENDENTE;
        return clienteRepository.findById(request.clienteId())
            .flatMap(cliente -> servicoRepository.findById(request.servicoId())
                .flatMap(servico -> staffRepository.findById(request.staffId())
                    .map(staff -> {
                        Agendamento entity = new Agendamento();
                        entity.setCliente(cliente);
                        entity.setServico(servico);
                        entity.setStaff(staff);
                        entity.setDataHora(request.dataHora());
                        Instant end = computeDataHoraFim(request.dataHora(), servico.getDuracaoMinutos());
                        entity.setDataHoraFim(end);
                        String tipo = request.tipo();
                        if (TIPO_ENCAIXE.equalsIgnoreCase(tipo)) {
                            List<Agendamento> overlapping = repository.findOverlappingByStaffOrderByDataHoraCreatedAt(staff.getId(), request.dataHora(), end);
                            if (overlapping.isEmpty()) {
                                tipo = TIPO_FIRME;
                            }
                        }
                        entity.setTipo(tipo);
                        entity.setStatus(status);
                        validateFirmeOverlap(tipo, staff.getId(), request.dataHora(), end, null);
                        Agendamento saved = repository.save(entity);
                        List<Agendamento> slotFila = repository.findOverlappingByStaffOrderByDataHoraCreatedAt(staff.getId(), saved.getDataHora(), saved.getDataHoraFim());
                        AgendamentoResponse response = toResponse(saved, slotFila.size());
                        auditLogService.log("POST /admin/agendamentos", null, response);
                        return response;
                    })));
    }

    public Optional<AgendamentoResponse> update(Long id, AgendamentoRequest request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.clienteId());
        Optional<Servico> servicoOpt = servicoRepository.findById(request.servicoId());
        Optional<Staff> staffOpt = staffRepository.findById(request.staffId());
        if (clienteOpt.isEmpty() || servicoOpt.isEmpty() || staffOpt.isEmpty()) {
            return Optional.empty();
        }
        Optional<AgendamentoResponse> beforeOpt = repository.findById(id).map(e -> toResponse(e, null));
        return repository.findById(id)
            .map(entity -> {
                entity.setCliente(clienteOpt.get());
                entity.setServico(servicoOpt.get());
                entity.setStaff(staffOpt.get());
                entity.setDataHora(request.dataHora());
                entity.setTipo(request.tipo());
                if (request.status() != null && !request.status().isBlank()) {
                    entity.setStatus(request.status());
                }
                Instant end = computeDataHoraFim(request.dataHora(), servicoOpt.get().getDuracaoMinutos());
                entity.setDataHoraFim(end);
                validateFirmeOverlap(request.tipo(), entity.getStaff().getId(), request.dataHora(), end, id);
                return repository.save(entity);
            })
            .map(e -> {
                AgendamentoResponse after = toResponse(e, null);
                auditLogService.log("PUT /admin/agendamentos", beforeOpt.orElse(null), after);
                return after;
            });
    }

    public boolean delete(Long id) {
        Optional<AgendamentoResponse> before = repository.findById(id).map(e -> toResponse(e, null));
        if (before.isEmpty()) return false;
        repository.deleteById(id);
        auditLogService.log("DELETE /admin/agendamentos", before.get(), null);
        return true;
    }

    @Transactional
    public Optional<AgendamentoResponse> updateStatus(Long id, String status) {
        Optional<AgendamentoResponse> beforeOpt = repository.findById(id).map(e -> toResponse(e, null));
        return repository.findById(id)
            .map(entity -> {
                entity.setStatus(status);
                return repository.save(entity);
            })
            .map(e -> {
                AgendamentoResponse after = toResponse(e, null);
                auditLogService.log("PATCH /admin/agendamentos/" + id + "/status", beforeOpt.orElse(null), after);
                return after;
            });
    }

    @Transactional
    public Optional<AgendamentoResponse> cancelByCliente(Long id, Long clienteId) {
        Optional<AgendamentoResponse> beforeOpt = repository.findById(id)
            .filter(entity -> entity.getCliente().getId().equals(clienteId))
            .map(e -> toResponse(e, null));
        return repository.findById(id)
            .filter(entity -> entity.getCliente().getId().equals(clienteId))
            .map(entity -> {
                entity.setStatus("CANCELADO");
                return repository.save(entity);
            })
            .map(e -> {
                AgendamentoResponse after = toResponse(e, null);
                auditLogService.log("PATCH /agendamentos/" + id + "/cancel", beforeOpt.orElse(null), after);
                return after;
            });
    }

    private static Instant computeDataHoraFim(Instant start, Integer duracaoMinutos) {
        int minutes = duracaoMinutos != null && duracaoMinutos > 0 ? duracaoMinutos : 30;
        return start.plus(Duration.ofMinutes(minutes));
    }

    private void validateFirmeOverlap(String tipo, Long staffId, Instant start, Instant end, Long excludeId) {
        if (tipo == null || !TIPO_FIRME.equalsIgnoreCase(tipo)) return;
        List<Agendamento> overlapping = repository.findOverlappingFirmeByStaff(staffId, start, end);
        boolean hasOther = overlapping.stream().anyMatch(a -> !a.getId().equals(excludeId));
        if (hasOther) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já ocupado para este staff (agendamento firme)");
        }
    }

    private AgendamentoResponse toResponse(Agendamento entity, Integer tamanhoFilaSlot) {
        Cliente c = entity.getCliente();
        Servico s = entity.getServico();
        Staff st = entity.getStaff();
        return new AgendamentoResponse(
            entity.getId(),
            c.getId(),
            c.getNome(),
            s != null ? s.getId() : null,
            s != null ? s.getTitulo() : null,
            st != null ? st.getId() : null,
            st != null ? st.getNome() : null,
            entity.getDataHora(),
            entity.getDataHoraFim(),
            entity.getTipo(),
            entity.getStatus(),
            entity.getCreatedAt(),
            tamanhoFilaSlot
        );
    }
}
