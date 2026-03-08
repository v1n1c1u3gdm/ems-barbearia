package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.emsbarbearia.entity.ConfiguracaoAgenda;
import com.emsbarbearia.entity.HorarioFuncionamento;
import com.emsbarbearia.repository.ConfiguracaoAgendaRepository;
import com.emsbarbearia.repository.HorarioFuncionamentoRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoAgendaServiceTest {

    @Mock
    ConfiguracaoAgendaRepository configuracaoAgendaRepository;

    @Mock
    HorarioFuncionamentoRepository horarioFuncionamentoRepository;

    @InjectMocks
    ConfiguracaoAgendaService service;

    @Test
    void getConfig_shouldReturnSlotAndHorariosFromRepositories() {
        var config = new ConfiguracaoAgenda();
        config.setId(1L);
        config.setSlotMinutos(30);
        when(configuracaoAgendaRepository.findById(1L)).thenReturn(Optional.of(config));

        var h = new HorarioFuncionamento();
        h.setDiaSemana(2);
        h.setAberto(true);
        h.setHoraInicio(LocalTime.of(9, 0));
        h.setHoraFim(LocalTime.of(19, 0));
        when(horarioFuncionamentoRepository.findAllByOrderByDiaSemanaAsc()).thenReturn(List.of(h));

        var result = service.getConfig();

        assertThat(result.slotMinutos()).isEqualTo(30);
        assertThat(result.horarios()).hasSize(1);
        assertThat(result.horarios().get(0).diaSemana()).isEqualTo(2);
        assertThat(result.horarios().get(0).aberto()).isTrue();
        assertThat(result.horarios().get(0).horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.horarios().get(0).horaFim()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void getConfig_shouldUseDefaultSlotWhenConfigNotFound() {
        when(configuracaoAgendaRepository.findById(1L)).thenReturn(Optional.empty());
        when(horarioFuncionamentoRepository.findAllByOrderByDiaSemanaAsc()).thenReturn(List.of());

        var result = service.getConfig();

        assertThat(result.slotMinutos()).isEqualTo(30);
        assertThat(result.horarios()).isEmpty();
    }
}
