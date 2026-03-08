package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.ClienteRequest;
import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.entity.Cliente;
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
class ClienteServiceTest {

    @Mock
    ClienteRepository repository;

    @InjectMocks
    ClienteService service;

    @Test
    void list_shouldReturnAllWhenNomeIsBlank() {
        Cliente entity = cliente(1L, "A", "a@x.com", null);
        when(repository.findAll()).thenReturn(List.of(entity));

        List<ClienteResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("A");
        assertThat(result.get(0).email()).isEqualTo("a@x.com");
    }

    @Test
    void list_shouldFilterByNomeWhenNomeGiven() {
        Cliente entity = cliente(1L, "Alessandra", "a@x.com", null);
        when(repository.findByNomeContainingIgnoreCase("aless")).thenReturn(List.of(entity));

        List<ClienteResponse> result = service.list("aless");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Alessandra");
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Cliente entity = cliente(1L, "Foo", "foo@x.com", "11999999999");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<ClienteResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().nome()).isEqualTo("Foo");
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        ClienteRequest request = new ClienteRequest("New", "new@x.com", null);
        Cliente saved = cliente(1L, "New", "new@x.com", null);
        when(repository.save(any(Cliente.class))).thenReturn(saved);

        ClienteResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nome()).isEqualTo("New");
        verify(repository).save(any(Cliente.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        ClienteRequest request = new ClienteRequest("X", "x@x.com", null);

        assertThat(service.update(999L, request)).isEmpty();
    }

    @Test
    void update_shouldSaveAndReturnResponseWhenFound() {
        Cliente existing = cliente(1L, "Old", "old@x.com", null);
        Cliente saved = cliente(1L, "Updated", "up@x.com", null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Cliente.class))).thenReturn(saved);
        ClienteRequest request = new ClienteRequest("Updated", "up@x.com", null);

        Optional<ClienteResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().nome()).isEqualTo("Updated");
        verify(repository).save(existing);
    }

    @Test
    void delete_shouldReturnFalseWhenNotExists() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThat(service.delete(999L)).isFalse();
        verify(repository).existsById(999L);
    }

    @Test
    void delete_shouldDeleteAndReturnTrueWhenExists() {
        when(repository.existsById(1L)).thenReturn(true);

        assertThat(service.delete(1L)).isTrue();
        verify(repository).deleteById(1L);
    }

    private static Cliente cliente(Long id, String nome, String email, String telefone) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome(nome);
        c.setEmail(email);
        c.setTelefone(telefone);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
