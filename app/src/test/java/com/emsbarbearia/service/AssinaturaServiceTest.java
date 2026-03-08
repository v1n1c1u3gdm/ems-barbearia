package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.AssinaturaRequest;
import com.emsbarbearia.dto.AssinaturaResponse;
import com.emsbarbearia.dto.ServicoSummary;
import com.emsbarbearia.entity.Assinatura;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.repository.AssinaturaRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.ServicoRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssinaturaServiceTest {

    @Mock
    AssinaturaRepository repository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ServicoRepository servicoRepository;

    @InjectMocks
    AssinaturaService service;

    @Test
    void list_shouldReturnAllWhenClienteIdNull() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNome("C");
        Assinatura a = new Assinatura();
        a.setId(1L);
        a.setCliente(cliente);
        a.setServicos(List.of());
        a.setCreatedAt(Instant.now());
        when(repository.findAll()).thenReturn(List.of(a));

        List<AssinaturaResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteNome()).isEqualTo("C");
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void create_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AssinaturaRequest request = new AssinaturaRequest(999L, List.of(1L));

        assertThat(service.create(request)).isEmpty();
    }

    @Test
    void create_shouldSaveAndReturnResponseWhenClienteExists() {
        Cliente c = new Cliente();
        c.setId(10L);
        c.setNome("Cliente");
        Servico s = new Servico();
        s.setId(1L);
        s.setTitulo("Corte");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        when(servicoRepository.findAllById(List.of(1L))).thenReturn(List.of(s));
        Assinatura saved = new Assinatura();
        saved.setId(1L);
        saved.setCliente(c);
        saved.setServicos(List.of(s));
        saved.setCreatedAt(Instant.now());
        when(repository.save(any(Assinatura.class))).thenReturn(saved);
        AssinaturaRequest request = new AssinaturaRequest(10L, List.of(1L));

        Optional<AssinaturaResponse> result = service.create(request);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().servicos()).containsExactly(new ServicoSummary(1L, "Corte"));
        verify(repository).save(any(Assinatura.class));
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

    @Test
    void list_shouldFilterByClienteIdWhenGiven() {
        Cliente c = new Cliente();
        c.setId(10L);
        c.setNome("Cliente");
        Assinatura a = new Assinatura();
        a.setId(1L);
        a.setCliente(c);
        a.setServicos(List.of());
        a.setCreatedAt(Instant.now());
        when(repository.findByClienteId(10L)).thenReturn(List.of(a));

        List<AssinaturaResponse> result = service.list(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clienteId()).isEqualTo(10L);
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Cliente c = new Cliente();
        c.setId(10L);
        c.setNome("Nome");
        Servico s = new Servico();
        s.setId(1L);
        s.setTitulo("Corte");
        Assinatura a = new Assinatura();
        a.setId(1L);
        a.setCliente(c);
        a.setServicos(List.of(s));
        a.setCreatedAt(Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        Optional<AssinaturaResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().servicos()).hasSize(1);
    }

    @Test
    void update_shouldReturnEmptyWhenClienteNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());
        AssinaturaRequest request = new AssinaturaRequest(999L, List.of(1L));

        assertThat(service.update(1L, request)).isEmpty();
    }

    @Test
    void update_shouldSaveAndReturnResponseWhenFound() {
        Cliente c = new Cliente();
        c.setId(10L);
        c.setNome("Cliente");
        Servico s = new Servico();
        s.setId(1L);
        s.setTitulo("Corte");
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(c));
        when(servicoRepository.findAllById(List.of(1L))).thenReturn(List.of(s));
        Assinatura existing = new Assinatura();
        existing.setId(1L);
        existing.setCliente(c);
        existing.setServicos(new ArrayList<>());
        existing.setCreatedAt(Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Assinatura.class))).thenReturn(existing);
        AssinaturaRequest request = new AssinaturaRequest(10L, List.of(1L));

        Optional<AssinaturaResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        verify(repository).save(any(Assinatura.class));
    }
}
