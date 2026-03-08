package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.entity.Agendamento;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.repository.AgendamentoRepository;
import com.emsbarbearia.repository.ClienteRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    AgendamentoRepository repository;

    @Mock
    ClienteRepository clienteRepository;

    @InjectMocks
    AgendamentoService service;

    @Test
    void list_shouldReturnAllWhenClienteIdNull() {
        Cliente cliente = cliente(10L, "C");
        Agendamento ag = agendamento(1L, cliente, Instant.now(), "Corte", "PENDENTE");
        when(repository.findAll()).thenReturn(List.of(ag));

        List<AgendamentoResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteNome()).isEqualTo("C");
        assertThat(result.get(0).status()).isEqualTo("PENDENTE");
    }

    @Test
    void list_shouldFilterByClienteIdWhenGiven() {
        Cliente cliente = cliente(10L, "Cliente");
        Agendamento ag = agendamento(1L, cliente, Instant.now(), null, "CONFIRMADO");
        when(repository.findByClienteId(10L)).thenReturn(List.of(ag));

        List<AgendamentoResponse> result = service.list(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteId()).isEqualTo(10L);
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Cliente c = cliente(10L, "Nome");
        Agendamento ag = agendamento(1L, c, Instant.now(), "Corte", "PENDENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(ag));

        Optional<AgendamentoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().clienteNome()).isEqualTo("Nome");
    }

    @Test
    void create_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(999L, Instant.now(), null, "PENDENTE");

        assertThat(service.create(request)).isEmpty();
    }

    @Test
    void create_shouldSaveAndReturnResponseWhenClienteExists() {
        Cliente c = cliente(10L, "Cliente");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        Agendamento saved = agendamento(1L, c, Instant.parse("2025-06-01T10:00:00Z"), "Corte", "PENDENTE");
        when(repository.save(any(Agendamento.class))).thenReturn(saved);
        AgendamentoRequest request = new AgendamentoRequest(10L, Instant.parse("2025-06-01T10:00:00Z"), "Corte", "PENDENTE");

        Optional<AgendamentoResponse> result = service.create(request);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().clienteNome()).isEqualTo("Cliente");
        verify(repository).save(any(Agendamento.class));
    }

    @Test
    void update_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AgendamentoRequest request = new AgendamentoRequest(999L, Instant.now(), null, "PENDENTE");

        assertThat(service.update(1L, request)).isEmpty();
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

    private static Agendamento agendamento(Long id, Cliente cliente, Instant dataHora, String servico, String status) {
        Agendamento a = new Agendamento();
        a.setId(id);
        a.setCliente(cliente);
        a.setDataHora(dataHora);
        a.setServico(servico);
        a.setStatus(status);
        a.setCreatedAt(Instant.now());
        return a;
    }
}
