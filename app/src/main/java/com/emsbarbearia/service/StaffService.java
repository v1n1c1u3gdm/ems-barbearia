package com.emsbarbearia.service;

import com.emsbarbearia.dto.HorarioFuncionamentoRequest;
import com.emsbarbearia.dto.HorarioFuncionamentoResponse;
import com.emsbarbearia.dto.StaffRequest;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.entity.Staff;
import com.emsbarbearia.entity.StaffDisponibilidade;
import com.emsbarbearia.repository.StaffDisponibilidadeRepository;
import com.emsbarbearia.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {
    private final StaffRepository repository;
    private final StaffDisponibilidadeRepository disponibilidadeRepository;

    public StaffService(StaffRepository repository, StaffDisponibilidadeRepository disponibilidadeRepository) {
        this.repository = repository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    public List<StaffResponse> list(String nome) {
        List<Staff> entities = nome != null && !nome.isBlank()
            ? repository.findByNomeContainingIgnoreCase(nome)
            : repository.findAll();
        return toResponseList(entities);
    }

    public List<StaffResponse> listAtivos() {
        return toResponseList(repository.findByAtivoTrue());
    }

    public Optional<StaffResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponseWithHorarios);
    }

    public StaffResponse create(StaffRequest request) {
        Staff entity = new Staff();
        entity.setNome(request.nome());
        entity.setAtivo(request.ativo() != null ? request.ativo() : true);
        Staff saved = repository.save(entity);
        saveHorarios(saved.getId(), request.horarios());
        return toResponseWithHorarios(saved);
    }

    public Optional<StaffResponse> update(Long id, StaffRequest request) {
        return repository.findById(id)
            .map(entity -> {
                entity.setNome(request.nome());
                if (request.ativo() != null) entity.setAtivo(request.ativo());
                Staff saved = repository.save(entity);
                if (request.horarios() != null) {
                    disponibilidadeRepository.deleteByStaffId(id);
                    saveHorarios(id, request.horarios());
                }
                return saved;
            })
            .map(this::toResponseWithHorarios);
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    private List<StaffResponse> toResponseList(List<Staff> entities) {
        if (entities.isEmpty()) return List.of();
        List<Long> ids = entities.stream().map(Staff::getId).toList();
        Map<Long, List<HorarioFuncionamentoResponse>> horariosByStaff = disponibilidadeRepository
            .findByStaffIdInOrderByStaffIdAscDiaSemanaAsc(ids)
            .stream()
            .collect(Collectors.groupingBy(StaffDisponibilidade::getStaffId,
                Collectors.mapping(this::horarioToResponse, Collectors.toList())));
        return entities.stream()
            .map(e -> toResponse(e, horariosByStaff.getOrDefault(e.getId(), List.of())))
            .toList();
    }

    private StaffResponse toResponseWithHorarios(Staff entity) {
        List<HorarioFuncionamentoResponse> horarios = disponibilidadeRepository
            .findByStaffIdOrderByDiaSemanaAsc(entity.getId())
            .stream()
            .map(this::horarioToResponse)
            .toList();
        return toResponse(entity, horarios);
    }

    private StaffResponse toResponse(Staff entity, List<HorarioFuncionamentoResponse> horarios) {
        return new StaffResponse(
            entity.getId(),
            entity.getNome(),
            entity.getAtivo(),
            entity.getCreatedAt(),
            horarios
        );
    }

    private HorarioFuncionamentoResponse horarioToResponse(StaffDisponibilidade d) {
        return new HorarioFuncionamentoResponse(
            d.getDiaSemana(),
            d.getAberto(),
            d.getHoraInicio(),
            d.getHoraFim()
        );
    }

    private void saveHorarios(Long staffId, List<HorarioFuncionamentoRequest> horarios) {
        if (horarios == null || horarios.isEmpty()) return;
        for (HorarioFuncionamentoRequest req : horarios) {
            if (req == null || req.diaSemana() == null) continue;
            StaffDisponibilidade d = new StaffDisponibilidade();
            d.setStaffId(staffId);
            d.setDiaSemana(req.diaSemana());
            d.setAberto(req.aberto() != null && req.aberto());
            d.setHoraInicio(req.horaInicio());
            d.setHoraFim(req.horaFim());
            disponibilidadeRepository.save(d);
        }
    }
}
