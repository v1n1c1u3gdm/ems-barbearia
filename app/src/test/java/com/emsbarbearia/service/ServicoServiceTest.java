package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.ServicoRequest;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.entity.Servico;
import com.emsbarbearia.repository.ServicoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    ServicoRepository repository;

    @InjectMocks
    ServicoService service;

    @Test
    void list_shouldReturnAllWhenTituloIsBlank() {
        Servico entity = servico(1L, "Corte", true, 30);
        when(repository.findAll()).thenReturn(List.of(entity));

        List<ServicoResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Corte");
        assertThat(result.get(0).ativo()).isTrue();
        assertThat(result.get(0).duracaoMinutos()).isEqualTo(30);
    }

    @Test
    void list_shouldFilterByTituloWhenTituloGiven() {
        Servico entity = servico(1L, "Barba", true, 45);
        when(repository.findByTituloContainingIgnoreCase("barba")).thenReturn(List.of(entity));

        List<ServicoResponse> result = service.list("barba");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Barba");
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Servico entity = servico(1L, "Corte", true, 30);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<ServicoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
    }

    @Test
    void create_shouldUseAtivoTrueAndDuracao30WhenRequestNulls() {
        ServicoRequest request = new ServicoRequest("New", null, null, null, null, null);
        Servico saved = servico(1L, "New", true, 30);
        when(repository.save(any(Servico.class))).thenReturn(saved);

        ServicoResponse result = service.create(request);

        assertThat(result.ativo()).isTrue();
        assertThat(result.duracaoMinutos()).isEqualTo(30);
        verify(repository).save(any(Servico.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        ServicoRequest request = new ServicoRequest("X", null, null, null, true, 45);
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

    private static Servico servico(Long id, String titulo, boolean ativo, Integer duracaoMinutos) {
        Servico s = new Servico();
        s.setId(id);
        s.setTitulo(titulo);
        s.setAtivo(ativo);
        s.setDuracaoMinutos(duracaoMinutos);
        s.setCreatedAt(Instant.now());
        return s;
    }
}
