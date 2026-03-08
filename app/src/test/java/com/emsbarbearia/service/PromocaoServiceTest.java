package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.PromocaoRequest;
import com.emsbarbearia.dto.PromocaoResponse;
import com.emsbarbearia.entity.Promocao;
import com.emsbarbearia.repository.PromocaoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromocaoServiceTest {

    @Mock
    PromocaoRepository repository;

    @InjectMocks
    PromocaoService service;

    @Test
    void list_shouldReturnAllWhenTituloIsBlank() {
        Promocao entity = promocao(1L, "Black Friday", true);
        when(repository.findAll()).thenReturn(List.of(entity));

        List<PromocaoResponse> result = service.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Black Friday");
        assertThat(result.get(0).ativo()).isTrue();
    }

    @Test
    void list_shouldFilterByTituloWhenTituloGiven() {
        Promocao entity = promocao(1L, "Promo", true);
        when(repository.findByTituloContainingIgnoreCase("promo")).thenReturn(List.of(entity));

        List<PromocaoResponse> result = service.list("promo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Promo");
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Promocao entity = promocao(1L, "Titulo", true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<PromocaoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
    }

    @Test
    void create_shouldUseAtivoTrueWhenRequestAtivoNull() {
        PromocaoRequest request = new PromocaoRequest("New", null, null, null, null);
        Promocao saved = promocao(1L, "New", true);
        when(repository.save(any(Promocao.class))).thenReturn(saved);

        PromocaoResponse result = service.create(request);

        assertThat(result.ativo()).isTrue();
        verify(repository).save(any(Promocao.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        PromocaoRequest request = new PromocaoRequest("X", null, null, null, true);
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

    private static Promocao promocao(Long id, String titulo, boolean ativo) {
        Promocao p = new Promocao();
        p.setId(id);
        p.setTitulo(titulo);
        p.setAtivo(ativo);
        p.setCreatedAt(Instant.now());
        return p;
    }
}
