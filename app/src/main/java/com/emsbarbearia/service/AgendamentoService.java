package com.emsbarbearia.service;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AgendamentoService {

    private static final String TIPO_FIRME = "FIRME";
    private static final String STATUS_PENDENTE = "PENDENTE";

    private final AgendamentoRepository repository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final StaffRepository staffRepository;

    public AgendamentoService(AgendamentoRepository repository, ClienteRepository clienteRepository,
                             ServicoRepository servicoRepository, StaffRepository staffRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.staffRepository = staffRepository;
    }

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
        return stream.map(this::toResponse).toList();
    }

    public Optional<AgendamentoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
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
                        entity.setTipo(request.tipo());
                        entity.setStatus(status);
                        Instant end = computeDataHoraFim(request.dataHora(), servico.getDuracaoMinutos());
                        entity.setDataHoraFim(end);
                        validateFirmeOverlap(request.tipo(), entity.getStaff().getId(), request.dataHora(), end, null);
                        return toResponse(repository.save(entity));
                    })));
    }

    public Optional<AgendamentoResponse> update(Long id, AgendamentoRequest request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.clienteId());
        Optional<Servico> servicoOpt = servicoRepository.findById(request.servicoId());
        Optional<Staff> staffOpt = staffRepository.findById(request.staffId());
        if (clienteOpt.isEmpty() || servicoOpt.isEmpty() || staffOpt.isEmpty()) {
            return Optional.empty();
        }
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
            .map(this::toResponse);
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    public Optional<AgendamentoResponse> updateStatus(Long id, String status) {
        return repository.findById(id)
            .map(entity -> {
                entity.setStatus(status);
                return repository.save(entity);
            })
            .map(this::toResponse);
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

    private AgendamentoResponse toResponse(Agendamento entity) {
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
            entity.getCreatedAt()
        );
    }
}
