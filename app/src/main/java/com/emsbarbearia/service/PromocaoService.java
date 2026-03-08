package com.emsbarbearia.service;

import com.emsbarbearia.dto.PromocaoRequest;
import com.emsbarbearia.dto.PromocaoResponse;
import com.emsbarbearia.entity.Promocao;
import com.emsbarbearia.repository.PromocaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PromocaoService {

    private final PromocaoRepository repository;

    public PromocaoService(PromocaoRepository repository) {
        this.repository = repository;
    }

    public List<PromocaoResponse> list(String titulo) {
        List<Promocao> entities = titulo != null && !titulo.isBlank()
            ? repository.findByTituloContainingIgnoreCase(titulo)
            : repository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public Optional<PromocaoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public PromocaoResponse create(PromocaoRequest request) {
        Promocao entity = new Promocao();
        entity.setTitulo(request.titulo());
        entity.setDescricao(request.descricao());
        entity.setValidoDe(request.validoDe());
        entity.setValidoAte(request.validoAte());
        entity.setAtivo(request.ativo() != null ? request.ativo() : true);
        return toResponse(repository.save(entity));
    }

    public Optional<PromocaoResponse> update(Long id, PromocaoRequest request) {
        return repository.findById(id)
            .map(entity -> {
                entity.setTitulo(request.titulo());
                entity.setDescricao(request.descricao());
                entity.setValidoDe(request.validoDe());
                entity.setValidoAte(request.validoAte());
                if (request.ativo() != null) {
                    entity.setAtivo(request.ativo());
                }
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

    private PromocaoResponse toResponse(Promocao entity) {
        return new PromocaoResponse(
            entity.getId(),
            entity.getTitulo(),
            entity.getDescricao(),
            entity.getValidoDe(),
            entity.getValidoAte(),
            entity.getAtivo(),
            entity.getCreatedAt()
        );
    }
}
