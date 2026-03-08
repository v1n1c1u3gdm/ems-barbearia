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

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
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
        return toResponse(repository.save(entity));
    }

    public Optional<ClienteResponse> update(Long id, ClienteRequest request) {
        return repository.findById(id)
            .map(entity -> {
                entity.setNome(request.nome());
                entity.setEmail(request.email());
                entity.setTelefone(request.telefone());
                return repository.save(entity);
            })
            .map(this::toResponse);
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
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
