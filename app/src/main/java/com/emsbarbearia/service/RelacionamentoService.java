package com.emsbarbearia.service;

import com.emsbarbearia.dto.RelacionamentoRequest;
import com.emsbarbearia.dto.RelacionamentoResponse;
import com.emsbarbearia.dto.RelacionamentoUpdateRequest;
import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.Relacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.RelacionamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RelacionamentoService {

    private final RelacionamentoRepository repository;
    private final ClienteRepository clienteRepository;
    private final AuditLogService auditLogService;

    public RelacionamentoService(RelacionamentoRepository repository, ClienteRepository clienteRepository,
                                 AuditLogService auditLogService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.auditLogService = auditLogService;
    }

    public List<RelacionamentoResponse> list(CanalRelacionamento canal, StatusRelacionamento status) {
        List<Relacionamento> entities;
        if (canal != null && status != null) {
            entities = repository.findByCanalAndStatus(canal, status);
        } else if (canal != null) {
            entities = repository.findByCanal(canal);
            if (status != null) {
                entities = entities.stream().filter(r -> r.getStatus() == status).toList();
            }
        } else if (status != null) {
            entities = repository.findByStatus(status);
        } else {
            entities = repository.findAllByOrderByCreatedAtDesc();
        }
        return entities.stream().map(this::toResponse).toList();
    }

    public Optional<RelacionamentoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public RelacionamentoResponse create(RelacionamentoRequest request) {
        Relacionamento entity = new Relacionamento();
        entity.setNome(request.nome());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        entity.setCanal(request.canal());
        entity.setStatus(request.status());
        entity.setDataUltimaInteracao(request.dataUltimaInteracao());
        entity.setTipoInteracao(request.tipoInteracao());
        if (request.clienteId() != null) {
            clienteRepository.findById(request.clienteId()).ifPresent(entity::setCliente);
        }
        RelacionamentoResponse response = toResponse(repository.save(entity));
        auditLogService.log("POST /admin/relacionamentos", null, response);
        return response;
    }

    public Optional<RelacionamentoResponse> update(Long id, RelacionamentoUpdateRequest request) {
        Optional<RelacionamentoResponse> beforeOpt = repository.findById(id).map(this::toResponse);
        return repository.findById(id)
            .map(entity -> {
                if (request.status() != null) {
                    entity.setStatus(request.status());
                }
                if (request.dataUltimaInteracao() != null) {
                    entity.setDataUltimaInteracao(request.dataUltimaInteracao());
                }
                if (request.tipoInteracao() != null) {
                    entity.setTipoInteracao(request.tipoInteracao());
                }
                return repository.save(entity);
            })
            .map(e -> {
                RelacionamentoResponse after = toResponse(e);
                auditLogService.log("PUT /admin/relacionamentos", beforeOpt.orElse(null), after);
                return after;
            });
    }

    public boolean delete(Long id) {
        Optional<RelacionamentoResponse> before = repository.findById(id).map(this::toResponse);
        if (before.isEmpty()) return false;
        repository.deleteById(id);
        auditLogService.log("DELETE /admin/relacionamentos", before.get(), null);
        return true;
    }

    private RelacionamentoResponse toResponse(Relacionamento entity) {
        Long clienteId = entity.getCliente() != null ? entity.getCliente().getId() : null;
        return new RelacionamentoResponse(
            entity.getId(),
            entity.getNome(),
            entity.getEmail(),
            entity.getTelefone(),
            entity.getCanal(),
            entity.getStatus(),
            entity.getDataUltimaInteracao(),
            entity.getTipoInteracao(),
            clienteId,
            entity.getCreatedAt()
        );
    }
}
