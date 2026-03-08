package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.entity.Agendamento;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.entity.Staff;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ServicoRepository;
import com.emsbarbearia.repository.StaffRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    AgendamentoRepository repository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ServicoRepository servicoRepository;

    @Mock
    StaffRepository staffRepository;

    @InjectMocks
    AgendamentoService service;

    @Test
    void list_shouldReturnAllWhenNoFilters() {
        Cliente cliente = cliente(10L, "C");
        Servico servico = servico(1L, "Corte", 30);
        Staff staff = staff(1L, "João");
        Agendamento ag = agendamento(1L, cliente, servico, staff, Instant.now(), "FIRME", "PENDENTE");
        when(repository.findAll()).thenReturn(List.of(ag));

        List<AgendamentoResponse> result = service.list(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteNome()).isEqualTo("C");
        assertThat(result.get(0).servicoTitulo()).isEqualTo("Corte");
        assertThat(result.get(0).staffNome()).isEqualTo("João");
        assertThat(result.get(0).status()).isEqualTo("PENDENTE");
    }

    @Test
    void list_shouldFilterByClienteIdWhenGiven() {
        Cliente cliente = cliente(10L, "Cliente");
        Servico servico = servico(1L, "Corte", 30);
        Staff staff = staff(1L, "João");
        Agendamento ag = agendamento(1L, cliente, servico, staff, Instant.now(), "FIRME", "CONFIRMADO");
        when(repository.findByClienteId(10L)).thenReturn(List.of(ag));

        List<AgendamentoResponse> result = service.list(10L, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteId()).isEqualTo(10L);
    }

    @Test
    void list_shouldUseDateRangeWhenDeAndAteGiven() {
        Instant de = Instant.parse("2025-06-01T00:00:00Z");
        Instant ate = Instant.parse("2025-06-02T00:00:00Z");
        Cliente cliente = cliente(10L, "C");
        Servico servico = servico(1L, "Corte", 30);
        Staff staff = staff(1L, "João");
        Agendamento ag = agendamento(1L, cliente, servico, staff, Instant.now(), "FIRME", "PENDENTE");
        when(repository.findByDataHoraBetweenOrderByDataHora(de, ate)).thenReturn(List.of(ag));

        List<AgendamentoResponse> result = service.list(null, null, null, de, ate);

        assertThat(result).hasSize(1);
        verify(repository).findByDataHoraBetweenOrderByDataHora(de, ate);
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Cliente c = cliente(10L, "Nome");
        Servico s = servico(1L, "Corte", 30);
        Staff st = staff(1L, "João");
        Agendamento ag = agendamento(1L, c, s, st, Instant.now(), "FIRME", "PENDENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(ag));

        Optional<AgendamentoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().clienteNome()).isEqualTo("Nome");
        assertThat(result.get().servicoTitulo()).isEqualTo("Corte");
        assertThat(result.get().staffNome()).isEqualTo("João");
    }

    @Test
    void create_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(999L, 1L, 1L, Instant.now(), "FIRME", null);

        assertThat(service.create(request)).isEmpty();
    }

    @Test
    void create_shouldReturnEmptyWhenServicoNotFound() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente(10L, "C")));
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(10L, 999L, 1L, Instant.now(), "FIRME", null);

        assertThat(service.create(request)).isEmpty();
    }

    @Test
    void create_shouldReturnEmptyWhenStaffNotFound() {
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente(10L, "C")));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico(1L, "Corte", 30)));
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(10L, 1L, 999L, Instant.now(), "FIRME", null);

        assertThat(service.create(request)).isEmpty();
    }

    @Test
    void create_shouldSaveAndReturnResponseWhenAllExist() {
        Cliente c = cliente(10L, "Cliente");
        Servico s = servico(1L, "Corte", 30);
        Staff st = staff(1L, "João");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(s));
        when(staffRepository.findById(1L)).thenReturn(Optional.of(st));
        when(repository.findOverlappingFirmeByStaff(eq(1L), any(Instant.class), any(Instant.class))).thenReturn(List.of());
        Agendamento saved = agendamento(1L, c, s, st, Instant.parse("2025-06-01T10:00:00Z"), "FIRME", "PENDENTE");
        when(repository.save(any(Agendamento.class))).thenReturn(saved);
        AgendamentoRequest request = new AgendamentoRequest(10L, 1L, 1L, Instant.parse("2025-06-01T10:00:00Z"), "FIRME", "PENDENTE");

        Optional<AgendamentoResponse> result = service.create(request);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().clienteNome()).isEqualTo("Cliente");
        verify(repository).save(any(Agendamento.class));
    }

    @Test
    void create_shouldThrowConflictWhenFirmeOverlaps() {
        Cliente c = cliente(10L, "C");
        Servico s = servico(1L, "Corte", 30);
        Staff st = staff(1L, "João");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(s));
        when(staffRepository.findById(1L)).thenReturn(Optional.of(st));
        Agendamento existing = agendamento(2L, c, s, st, Instant.parse("2025-06-01T10:00:00Z"), "FIRME", "APROVADO");
        when(repository.findOverlappingFirmeByStaff(eq(1L), any(Instant.class), any(Instant.class))).thenReturn(List.of(existing));
        AgendamentoRequest request = new AgendamentoRequest(10L, 1L, 1L, Instant.parse("2025-06-01T10:00:00Z"), "FIRME", null);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Horário já ocupado");
    }

    @Test
    void update_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(999L, 1L, 1L, Instant.now(), "FIRME", "PENDENTE");

        assertThat(service.update(1L, request)).isEmpty();
    }

    @Test
    void update_shouldSaveAndReturnResponseWhenFound() {
        Cliente c = cliente(10L, "C");
        Servico s = servico(1L, "Corte", 30);
        Staff st = staff(1L, "João");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(s));
        when(staffRepository.findById(1L)).thenReturn(Optional.of(st));
        Agendamento existing = agendamento(1L, c, s, st, Instant.now(), "FIRME", "PENDENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findOverlappingFirmeByStaff(eq(1L), any(Instant.class), any(Instant.class))).thenReturn(List.of(existing));
        when(repository.save(any(Agendamento.class))).thenReturn(existing);
        AgendamentoRequest request = new AgendamentoRequest(10L, 1L, 1L, Instant.parse("2025-06-01T11:00:00Z"), "FIRME", "APROVADO");

        Optional<AgendamentoResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        verify(repository).save(any(Agendamento.class));
    }

    @Test
    void updateStatus_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.updateStatus(999L, "APROVADO")).isEmpty();
    }

    @Test
    void updateStatus_shouldUpdateAndReturnResponse() {
        Cliente c = cliente(10L, "C");
        Servico s = servico(1L, "Corte", 30);
        Staff st = staff(1L, "João");
        Agendamento ag = agendamento(1L, c, s, st, Instant.now(), "FIRME", "PENDENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(ag));
        ag.setStatus("APROVADO");
        when(repository.save(any(Agendamento.class))).thenReturn(ag);

        Optional<AgendamentoResponse> result = service.updateStatus(1L, "APROVADO");

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo("APROVADO");
    }

    @Test
    void delete_shouldReturnFalseWhenNotExists() {
        when(repository.existsById(999L)).thenReturn(false);
        assertThat(service.delete(999L)).isFalse();
    }

    @Test
    void delete_shouldDeleteAndReturnTrueWhenExists() {
        when(repository.existsById(1L)).thenReturn(true);
        assertThat(service.delete(1L)).isTrue();
        verify(repository).deleteById(1L);
    }

    private static Cliente cliente(Long id, String nome) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome(nome);
        c.setEmail("e@x.com");
        c.setCreatedAt(Instant.now());
        return c;
    }

    private static Servico servico(Long id, String titulo, int duracaoMinutos) {
        Servico s = new Servico();
        s.setId(id);
        s.setTitulo(titulo);
        s.setDuracaoMinutos(duracaoMinutos);
        s.setCreatedAt(Instant.now());
        return s;
    }

    private static Staff staff(Long id, String nome) {
        Staff st = new Staff();
        st.setId(id);
        st.setNome(nome);
        st.setCreatedAt(Instant.now());
        return st;
    }

    private static Agendamento agendamento(Long id, Cliente cliente, Servico servico, Staff staff, Instant dataHora, String tipo, String status) {
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setCliente(cliente);
        a.setServico(servico);
        a.setStaff(staff);
        a.setDataHora(dataHora);
        a.setDataHoraFim(dataHora.plusSeconds(1800));
        a.setTipo(tipo);
        a.setStatus(status);
        a.setCreatedAt(Instant.now());
        return a;
    }
}
