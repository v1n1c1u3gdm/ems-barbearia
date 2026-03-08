package com.emsbarbearia.service;

import com.emsbarbearia.dto.ConfiguracaoAgendaResponse;
import com.emsbarbearia.dto.HorarioFuncionamentoResponse;
import com.emsbarbearia.entity.ConfiguracaoAgenda;
import com.emsbarbearia.entity.HorarioFuncionamento;
import com.emsbarbearia.repository.ConfiguracaoAgendaRepository;
import com.emsbarbearia.repository.HorarioFuncionamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfiguracaoAgendaService {

    private static final long DEFAULT_CONFIG_ID = 1L;
    private static final int DEFAULT_SLOT_MINUTOS = 30;

    private final ConfiguracaoAgendaRepository configuracaoAgendaRepository;
    private final HorarioFuncionamentoRepository horarioFuncionamentoRepository;

    public ConfiguracaoAgendaService(
        ConfiguracaoAgendaRepository configuracaoAgendaRepository,
        HorarioFuncionamentoRepository horarioFuncionamentoRepository
    ) {
        this.configuracaoAgendaRepository = configuracaoAgendaRepository;
        this.horarioFuncionamentoRepository = horarioFuncionamentoRepository;
    }

    public ConfiguracaoAgendaResponse getConfig() {
        int slotMinutos = configuracaoAgendaRepository.findById(DEFAULT_CONFIG_ID)
            .map(ConfiguracaoAgenda::getSlotMinutos)
            .orElse(DEFAULT_SLOT_MINUTOS);
        List<HorarioFuncionamentoResponse> horarios = horarioFuncionamentoRepository
            .findAllByOrderByDiaSemanaAsc()
            .stream()
            .map(this::toResponse)
            .toList();
        return new ConfiguracaoAgendaResponse(slotMinutos, horarios);
    }

    private HorarioFuncionamentoResponse toResponse(HorarioFuncionamento h) {
        return new HorarioFuncionamentoResponse(
            h.getDiaSemana(),
            h.getAberto(),
            h.getHoraInicio(),
            h.getHoraFim()
        );
    }
}
