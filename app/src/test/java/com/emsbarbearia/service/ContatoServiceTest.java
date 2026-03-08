package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.ContatoRequest;
import com.emsbarbearia.dto.ContatoResponse;
import com.emsbarbearia.entity.Contato;
import com.emsbarbearia.repository.ContatoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContatoServiceTest {

    @Mock
    ContatoRepository repository;

    @InjectMocks
    ContatoService service;

    @Test
    void list_shouldReturnAllWhenNomeIsBlank() {
        Contato entity = contato(1L, "A", "a@x.com", null, "msg");
        when(repository.findAll()).thenReturn(List.of(entity));

        List<ContatoResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("A");
        assertThat(result.get(0).mensagem()).isEqualTo("msg");
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Contato entity = contato(1L, "Foo", "foo@x.com", null, "hi");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<ContatoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().nome()).isEqualTo("Foo");
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        ContatoRequest request = new ContatoRequest("New", "new@x.com", null, "msg");
        Contato saved = contato(1L, "New", "new@x.com", null, "msg");
        when(repository.save(any(Contato.class))).thenReturn(saved);

        ContatoResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Contato.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        ContatoRequest request = new ContatoRequest("X", "x@x.com", null, null);
        assertThat(service.update(999L, request)).isEmpty();
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

    private static Contato contato(Long id, String nome, String email, String telefone, String mensagem) {
        Contato c = new Contato();
        c.setId(id);
        c.setNome(nome);
        c.setEmail(email);
        c.setTelefone(telefone);
        c.setMensagem(mensagem);
        c.setCreatedAt(Instant.now());
        return c;
    }
}
