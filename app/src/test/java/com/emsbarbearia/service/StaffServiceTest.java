package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.emsbarbearia.entity.Staff;
import com.emsbarbearia.entity.StaffDisponibilidade;
import com.emsbarbearia.repository.StaffDisponibilidadeRepository;
import com.emsbarbearia.repository.StaffRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    StaffRepository staffRepository;

    @Mock
    StaffDisponibilidadeRepository disponibilidadeRepository;

    @InjectMocks
    StaffService service;

    @Test
    void listAtivos_shouldReturnStaffWithHorariosFromDisponibilidade() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setNome("Emerson");
        staff.setAtivo(true);
        staff.setCreatedAt(Instant.now());
        when(staffRepository.findByAtivoTrue()).thenReturn(List.of(staff));

        StaffDisponibilidade disp = new StaffDisponibilidade();
        disp.setStaffId(1L);
        disp.setDiaSemana(2);
        disp.setAberto(true);
        disp.setHoraInicio(LocalTime.of(9, 0));
        disp.setHoraFim(LocalTime.of(19, 0));
        when(disponibilidadeRepository.findByStaffIdInOrderByStaffIdAscDiaSemanaAsc(anyList()))
            .thenReturn(List.of(disp));

        var result = service.listAtivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Emerson");
        assertThat(result.get(0).horarios()).hasSize(1);
        assertThat(result.get(0).horarios().get(0).diaSemana()).isEqualTo(2);
        assertThat(result.get(0).horarios().get(0).aberto()).isTrue();
        assertThat(result.get(0).horarios().get(0).horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).horarios().get(0).horaFim()).isEqualTo(LocalTime.of(19, 0));
    }
}
