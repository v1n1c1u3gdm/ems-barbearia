package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ContatoRepository;
import com.emsbarbearia.repository.PromocaoRepository;
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
    PromocaoRepository promocaoRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void getSummary_shouldReturnCountsFromRepositories() {
        when(contatoRepository.count()).thenReturn(10L);
        when(clienteRepository.count()).thenReturn(5L);
        when(agendamentoRepository.count()).thenReturn(3L);
        when(promocaoRepository.count()).thenReturn(2L);

        var result = service.getSummary();

        assertThat(result.contatos()).isEqualTo(10L);
        assertThat(result.clientes()).isEqualTo(5L);
        assertThat(result.agendamentos()).isEqualTo(3L);
        assertThat(result.promocoes()).isEqualTo(2L);
    }
}
