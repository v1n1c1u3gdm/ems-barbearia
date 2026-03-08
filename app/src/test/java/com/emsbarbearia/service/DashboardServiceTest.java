package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ContatoRepository;
import com.emsbarbearia.repository.ServicoRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    ContatoRepository contatoRepository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    AgendamentoRepository agendamentoRepository;

    @Mock
    ServicoRepository servicoRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void getSummary_shouldReturnCountsAndLatestUpdateFromRepositories() {
        when(contatoRepository.count()).thenReturn(10L);
        when(clienteRepository.count()).thenReturn(5L);
        when(agendamentoRepository.count()).thenReturn(3L);
        when(servicoRepository.count()).thenReturn(2L);
        Instant older = Instant.parse("2025-03-01T10:00:00Z");
        Instant latest = Instant.parse("2025-03-07T14:00:00Z");
        when(contatoRepository.findLatestCreatedAt()).thenReturn(Optional.of(older));
        when(clienteRepository.findLatestCreatedAt()).thenReturn(Optional.of(latest));
        when(agendamentoRepository.findLatestCreatedAt()).thenReturn(Optional.empty());
        when(servicoRepository.findLatestCreatedAt()).thenReturn(Optional.of(older));

        var result = service.getSummary();

        assertThat(result.contatos()).isEqualTo(10L);
        assertThat(result.clientes()).isEqualTo(5L);
        assertThat(result.agendamentos()).isEqualTo(3L);
        assertThat(result.servicos()).isEqualTo(2L);
        assertThat(result.ultimaAtualizacao()).isEqualTo(latest);
    }

    @Test
    void getSummary_shouldReturnNullUltimaAtualizacaoWhenNoData() {
        when(contatoRepository.count()).thenReturn(0L);
        when(clienteRepository.count()).thenReturn(0L);
        when(agendamentoRepository.count()).thenReturn(0L);
        when(servicoRepository.count()).thenReturn(0L);
        when(contatoRepository.findLatestCreatedAt()).thenReturn(Optional.empty());
        when(clienteRepository.findLatestCreatedAt()).thenReturn(Optional.empty());
        when(agendamentoRepository.findLatestCreatedAt()).thenReturn(Optional.empty());
        when(servicoRepository.findLatestCreatedAt()).thenReturn(Optional.empty());

        var result = service.getSummary();

        assertThat(result.ultimaAtualizacao()).isNull();
    }
}
