package com.emsbarbearia.service;

import com.emsbarbearia.dto.ContatoRequest;
import com.emsbarbearia.dto.ContatoResponse;
import com.emsbarbearia.entity.Contato;
import com.emsbarbearia.repository.ContatoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContatoService {

    private final ContatoRepository repository;

    public ContatoService(ContatoRepository repository) {
        this.repository = repository;
    }

    public List<ContatoResponse> list(String nome) {
        List<Contato> entities = nome != null && !nome.isBlank()
            ? repository.findByNomeContainingIgnoreCase(nome)
            : repository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public Optional<ContatoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public ContatoResponse create(ContatoRequest request) {
        Contato entity = new Contato();
        entity.setNome(request.nome());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        entity.setMensagem(request.mensagem());
        return toResponse(repository.save(entity));
    }

    public Optional<ContatoResponse> update(Long id, ContatoRequest request) {
        return repository.findById(id)
            .map(entity -> {
                entity.setNome(request.nome());
                entity.setEmail(request.email());
                entity.setTelefone(request.telefone());
                entity.setMensagem(request.mensagem());
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

    private ContatoResponse toResponse(Contato entity) {
        return new ContatoResponse(
            entity.getId(),
            entity.getNome(),
            entity.getEmail(),
            entity.getTelefone(),
            entity.getMensagem(),
            entity.getCreatedAt()
        );
    }
}
