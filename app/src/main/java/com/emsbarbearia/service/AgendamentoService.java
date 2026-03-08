package com.emsbarbearia.service;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.entity.Agendamento;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final ClienteRepository clienteRepository;

    public AgendamentoService(AgendamentoRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    public List<AgendamentoResponse> list(Long clienteId) {
        List<Agendamento> entities = clienteId != null
            ? repository.findByClienteId(clienteId)
            : repository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public Optional<AgendamentoResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public Optional<AgendamentoResponse> create(AgendamentoRequest request) {
        return clienteRepository.findById(request.clienteId())
            .map(cliente -> {
                Agendamento entity = new Agendamento();
                entity.setCliente(cliente);
                entity.setDataHora(request.dataHora());
                entity.setServico(request.servico());
                entity.setStatus(request.status());
                return toResponse(repository.save(entity));
            });
    }

    public Optional<AgendamentoResponse> update(Long id, AgendamentoRequest request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.clienteId());
        if (clienteOpt.isEmpty()) {
            return Optional.empty();
        }
        return repository.findById(id)
            .map(entity -> {
                entity.setCliente(clienteOpt.get());
                entity.setDataHora(request.dataHora());
                entity.setServico(request.servico());
                entity.setStatus(request.status());
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

    private AgendamentoResponse toResponse(Agendamento entity) {
        Cliente c = entity.getCliente();
        return new AgendamentoResponse(
            entity.getId(),
            c.getId(),
            c.getNome(),
            entity.getDataHora(),
            entity.getServico(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }
}
