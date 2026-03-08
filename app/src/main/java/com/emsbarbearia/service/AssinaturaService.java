package com.emsbarbearia.service;

import com.emsbarbearia.dto.AssinaturaRequest;
import com.emsbarbearia.dto.AssinaturaResponse;
import com.emsbarbearia.dto.ServicoSummary;
import com.emsbarbearia.entity.Assinatura;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.repository.AssinaturaRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AssinaturaService {

    private final AssinaturaRepository repository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public AssinaturaService(AssinaturaRepository repository, ClienteRepository clienteRepository,
                            ServicoRepository servicoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    public List<AssinaturaResponse> list(Long clienteId) {
        Stream<Assinatura> stream = clienteId != null
            ? repository.findByClienteId(clienteId).stream()
            : repository.findAll().stream();
        return stream.map(this::toResponse).toList();
    }

    public Optional<AssinaturaResponse> getById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public Optional<AssinaturaResponse> create(AssinaturaRequest request) {
        return clienteRepository.findById(request.clienteId())
            .map(cliente -> {
                Assinatura entity = new Assinatura();
                entity.setCliente(cliente);
                List<Servico> servicos = request.servicoIds() != null
                    ? servicoRepository.findAllById(request.servicoIds())
                    : new ArrayList<>();
                entity.setServicos(servicos);
                return toResponse(repository.save(entity));
            });
    }

    public Optional<AssinaturaResponse> update(Long id, AssinaturaRequest request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.clienteId());
        if (clienteOpt.isEmpty()) return Optional.empty();
        return repository.findById(id)
            .map(entity -> {
                entity.setCliente(clienteOpt.get());
                List<Servico> servicos = request.servicoIds() != null
                    ? servicoRepository.findAllById(request.servicoIds())
                    : new ArrayList<>();
                entity.setServicos(servicos);
                return repository.save(entity);
            })
            .map(this::toResponse);
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    private AssinaturaResponse toResponse(Assinatura entity) {
        List<ServicoSummary> summaries = entity.getServicos().stream()
            .map(s -> new ServicoSummary(s.getId(), s.getTitulo()))
            .toList();
        return new AssinaturaResponse(
            entity.getId(),
            entity.getCliente().getId(),
            entity.getCliente().getNome(),
            summaries,
            entity.getCreatedAt()
        );
    }
}
