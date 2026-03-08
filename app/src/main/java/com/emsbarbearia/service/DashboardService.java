package com.emsbarbearia.service;

import com.emsbarbearia.dto.DashboardSummaryResponse;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ContatoRepository;
import com.emsbarbearia.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final ContatoRepository contatoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;

    public DashboardService(
        ContatoRepository contatoRepository,
        ClienteRepository clienteRepository,
        AgendamentoRepository agendamentoRepository,
        ServicoRepository servicoRepository
    ) {
        this.contatoRepository = contatoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
    }

    public DashboardSummaryResponse getSummary() {
        Instant ultima = Stream.of(
            contatoRepository.findLatestCreatedAt(),
            clienteRepository.findLatestCreatedAt(),
            agendamentoRepository.findLatestCreatedAt(),
            servicoRepository.findLatestCreatedAt()
        )
            .flatMap(Optional::stream)
            .max(Instant::compareTo)
            .orElse(null);
        return new DashboardSummaryResponse(
            contatoRepository.count(),
            clienteRepository.count(),
            agendamentoRepository.count(),
            servicoRepository.count(),
            ultima
        );
    }
}
