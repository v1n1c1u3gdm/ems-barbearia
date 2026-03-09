package com.emsbarbearia.service;

import com.emsbarbearia.dto.ServicoRequest;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {
    private final ServicoRepository repository;
    private final AuditLogService auditLogService;

    public ServicoService(ServicoRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public List<ServicoResponse> list(String titulo) {
        List<Servico> entities = titulo != null && !titulo.isBlank()
            ? repository.findByTituloContainingIgnoreCase(titulo)
            : repository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public List<ServicoResponse> listAtivos() {
        return repository.findByAtivoTrue().stream().map(this::toResponse).toList();
    }

    public Optional<ServicoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public ServicoResponse create(ServicoRequest request) {
        Servico entity = new Servico();
        entity.setTitulo(request.titulo());
        entity.setDescricao(request.descricao());
        entity.setValidoDe(request.validoDe());
        entity.setValidoAte(request.validoAte());
        entity.setAtivo(request.ativo() != null ? request.ativo() : true);
        entity.setDuracaoMinutos(request.duracaoMinutos() != null ? request.duracaoMinutos() : 30);
        ServicoResponse response = toResponse(repository.save(entity));
        auditLogService.log("POST /admin/servicos", null, response);
        return response;
    }

    public Optional<ServicoResponse> update(Long id, ServicoRequest request) {
        Optional<ServicoResponse> beforeOpt = repository.findById(id).map(this::toResponse);
        return repository.findById(id)
            .map(entity -> {
                entity.setTitulo(request.titulo());
                entity.setDescricao(request.descricao());
                entity.setValidoDe(request.validoDe());
                entity.setValidoAte(request.validoAte());
                if (request.ativo() != null) entity.setAtivo(request.ativo());
                if (request.duracaoMinutos() != null) entity.setDuracaoMinutos(request.duracaoMinutos());
                return repository.save(entity);
            })
            .map(e -> {
                ServicoResponse after = toResponse(e);
                auditLogService.log("PUT /admin/servicos", beforeOpt.orElse(null), after);
                return after;
            });
    }

    public boolean delete(Long id) {
        Optional<ServicoResponse> before = repository.findById(id).map(this::toResponse);
        if (before.isEmpty()) return false;
        repository.deleteById(id);
        auditLogService.log("DELETE /admin/servicos", before.get(), null);
        return true;
    }

    private ServicoResponse toResponse(Servico entity) {
        return new ServicoResponse(
            entity.getId(),
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getValidoDe(),
            entity.getValidoAte(),
            entity.getAtivo(),
            entity.getDuracaoMinutos(),
            entity.getCreatedAt()
        );
    }
}
