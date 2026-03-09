package com.emsbarbearia.service;

import com.emsbarbearia.dto.ClienteRequest;
import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final AuditLogService auditLogService;

    public ClienteService(ClienteRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public List<ClienteResponse> list(String nome) {
        List<Cliente> entities = nome != null && !nome.isBlank()
            ? repository.findByNomeContainingIgnoreCase(nome)
            : repository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public Optional<ClienteResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public ClienteResponse create(ClienteRequest request) {
        Cliente entity = new Cliente();
        entity.setNome(request.nome());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        ClienteResponse response = toResponse(repository.save(entity));
        auditLogService.log("POST /admin/clientes", null, response);
        return response;
    }

    public Optional<ClienteResponse> update(Long id, ClienteRequest request) {
        Optional<ClienteResponse> beforeOpt = repository.findById(id).map(this::toResponse);
        return repository.findById(id)
            .map(entity -> {
                entity.setNome(request.nome());
                entity.setEmail(request.email());
                entity.setTelefone(request.telefone());
                return repository.save(entity);
            })
            .map(e -> {
                ClienteResponse after = toResponse(e);
                auditLogService.log("PUT /admin/clientes", beforeOpt.orElse(null), after);
                return after;
            });
    }

    public boolean delete(Long id) {
        Optional<ClienteResponse> before = repository.findById(id).map(this::toResponse);
        if (before.isEmpty()) return false;
        repository.deleteById(id);
        auditLogService.log("DELETE /admin/clientes", before.get(), null);
        return true;
    }

    private ClienteResponse toResponse(Cliente entity) {
        return new ClienteResponse(
            entity.getId(),
            entity.getNome(),
            entity.getEmail(),
            entity.getTelefone(),
            entity.getCreatedAt()
        );
    }
}
