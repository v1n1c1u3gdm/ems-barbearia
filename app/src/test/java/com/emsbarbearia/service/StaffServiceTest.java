package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.HorarioFuncionamentoRequest;
import com.emsbarbearia.dto.StaffRequest;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.entity.Staff;
import com.emsbarbearia.entity.StaffDisponibilidade;
import com.emsbarbearia.repository.StaffDisponibilidadeRepository;
import com.emsbarbearia.repository.StaffRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    StaffService service;

    @Test
    void list_shouldReturnAllWhenNomeIsBlank() {
        Staff staff = staff(1L, "João", true);
        when(staffRepository.findAll()).thenReturn(List.of(staff));
        when(disponibilidadeRepository.findByStaffIdInOrderByStaffIdAscDiaSemanaAsc(anyList())).thenReturn(List.of());

        List<StaffResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("João");
    }

    @Test
    void list_shouldFilterByNomeWhenNomeGiven() {
        Staff staff = staff(1L, "Maria", true);
        when(staffRepository.findByNomeContainingIgnoreCase("mar")).thenReturn(List.of(staff));
        when(disponibilidadeRepository.findByStaffIdInOrderByStaffIdAscDiaSemanaAsc(anyList())).thenReturn(List.of());

        List<StaffResponse> result = service.list("mar");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Maria");
    }

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

        List<StaffResponse> result = service.listAtivos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Emerson");
        assertThat(result.get(0).horarios()).hasSize(1);
        assertThat(result.get(0).horarios().get(0).diaSemana()).isEqualTo(2);
        assertThat(result.get(0).horarios().get(0).aberto()).isTrue();
        assertThat(result.get(0).horarios().get(0).horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).horarios().get(0).horaFim()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWithHorariosWhenFound() {
        Staff staff = staff(1L, "João", true);
        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        StaffDisponibilidade disp = new StaffDisponibilidade();
        disp.setStaffId(1L);
        disp.setDiaSemana(1);
        disp.setAberto(true);
        disp.setHoraInicio(LocalTime.of(8, 0));
        disp.setHoraFim(LocalTime.of(18, 0));
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of(disp));

        Optional<StaffResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().horarios()).hasSize(1);
    }

    @Test
    void create_shouldSaveStaffAndHorariosAndLog() {
        StaffRequest request = new StaffRequest("Novo", true, List.of(
            new HorarioFuncionamentoRequest(1, true, LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));
        Staff saved = staff(1L, "Novo", true);
        when(staffRepository.save(any(Staff.class))).thenReturn(saved);
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of());

        StaffResponse result = service.create(request);

        assertThat(result.nome()).isEqualTo("Novo");
        verify(staffRepository).save(any(Staff.class));
        verify(disponibilidadeRepository).save(any(StaffDisponibilidade.class));
        verify(auditLogService).log(eq("POST /admin/staff"), eq(null), any());
    }

    @Test
    void create_shouldUseAtivoTrueWhenNull() {
        StaffRequest request = new StaffRequest("Nome", null, null);
        Staff saved = staff(1L, "Nome", true);
        when(staffRepository.save(any(Staff.class))).thenReturn(saved);
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of());

        StaffResponse result = service.create(request);

        assertThat(result.ativo()).isTrue();
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        StaffRequest request = new StaffRequest("X", true, null);

        assertThat(service.update(999L, request)).isEmpty();
    }

    @Test
    void update_shouldSaveAndLogWhenFound() {
        Staff entity = staff(1L, "Antigo", true);
        when(staffRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(staffRepository.save(any(Staff.class))).thenAnswer(inv -> inv.getArgument(0));
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of());

        StaffRequest request = new StaffRequest("Atualizado", false, null);
        Optional<StaffResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().nome()).isEqualTo("Atualizado");
        verify(auditLogService).log(eq("PUT /admin/staff"), any(), any());
    }

    @Test
    void update_shouldReplaceHorariosWhenHorariosProvided() {
        Staff entity = staff(1L, "Staff", true);
        when(staffRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(staffRepository.save(any(Staff.class))).thenAnswer(inv -> inv.getArgument(0));
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of());

        StaffRequest request = new StaffRequest("Staff", true, List.of(
            new HorarioFuncionamentoRequest(2, true, LocalTime.of(10, 0), LocalTime.of(18, 0))
        ));
        Optional<StaffResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        verify(disponibilidadeRepository).deleteByStaffId(1L);
        verify(disponibilidadeRepository).save(any(StaffDisponibilidade.class));
    }

    @Test
    void delete_shouldReturnFalseWhenNotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.delete(999L)).isFalse();
    }

    @Test
    void delete_shouldDeleteAndLogWhenFound() {
        Staff entity = staff(1L, "João", true);
        when(staffRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(disponibilidadeRepository.findByStaffIdOrderByDiaSemanaAsc(1L)).thenReturn(List.of());

        assertThat(service.delete(1L)).isTrue();
        verify(staffRepository).deleteById(1L);
        verify(auditLogService).log(eq("DELETE /admin/staff"), any(), eq(null));
    }

    private static Staff staff(Long id, String nome, boolean ativo) {
        Staff s = new Staff();
        s.setId(id);
        s.setNome(nome);
        s.setAtivo(ativo);
        s.setCreatedAt(Instant.now());
        return s;
    }
}
