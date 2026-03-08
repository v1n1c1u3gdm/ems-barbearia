package com.emsbarbearia.service;

import com.emsbarbearia.dto.DashboardSummaryResponse;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ContatoRepository;
import com.emsbarbearia.repository.PromocaoRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ContatoRepository contatoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PromocaoRepository promocaoRepository;

    public DashboardService(
        ContatoRepository contatoRepository,
        ClienteRepository clienteRepository,
        AgendamentoRepository agendamentoRepository,
        PromocaoRepository promocaoRepository
    ) {
        this.contatoRepository = contatoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.promocaoRepository = promocaoRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
            contatoRepository.count(),
            clienteRepository.count(),
            agendamentoRepository.count(),
            promocaoRepository.count()
        );
    }
}
