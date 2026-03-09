package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.RelacionamentoRequest;
import com.emsbarbearia.dto.RelacionamentoResponse;
import com.emsbarbearia.dto.RelacionamentoUpdateRequest;
import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.Relacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.entity.TipoInteracao;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.RelacionamentoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelacionamentoServiceTest {

    @Mock
    RelacionamentoRepository repository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    RelacionamentoService service;

    @Test
    void list_shouldReturnAllWhenNoFilter() {
        Relacionamento entity = relacionamento(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        List<RelacionamentoResponse> result = service.list(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).canal()).isEqualTo(CanalRelacionamento.EMAIL);
        assertThat(result.get(0).status()).isEqualTo(StatusRelacionamento.QUENTE);
    }

    @Test
    void list_shouldReturnFilteredByCanalAndStatus() {
        Relacionamento entity = relacionamento(1L, CanalRelacionamento.WHATSAPP, StatusRelacionamento.MORNO);
        when(repository.findByCanalAndStatus(CanalRelacionamento.WHATSAPP, StatusRelacionamento.MORNO))
            .thenReturn(List.of(entity));

        List<RelacionamentoResponse> result = service.list(CanalRelacionamento.WHATSAPP, StatusRelacionamento.MORNO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).canal()).isEqualTo(CanalRelacionamento.WHATSAPP);
        assertThat(result.get(0).status()).isEqualTo(StatusRelacionamento.MORNO);
    }

    @Test
    void getById_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.getById(999L)).isEmpty();
    }

    @Test
    void getById_shouldReturnResponseWhenFound() {
        Relacionamento entity = relacionamento(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<RelacionamentoResponse> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().nome()).isEqualTo("Foo");
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        RelacionamentoRequest request = new RelacionamentoRequest(
            "New", "new@x.com", null,
            CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE,
            null, TipoInteracao.MOTIVADA_PELO_CLIENTE, null);
        Relacionamento saved = relacionamento(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(repository.save(any(Relacionamento.class))).thenReturn(saved);

        RelacionamentoResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Relacionamento.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        RelacionamentoUpdateRequest request = new RelacionamentoUpdateRequest(StatusRelacionamento.MORNO, null, null);
        assertThat(service.update(999L, request)).isEmpty();
    }

    @Test
    void update_shouldUpdateStatusWhenProvided() {
        Relacionamento entity = relacionamento(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Relacionamento.class))).thenAnswer(inv -> inv.getArgument(0));
        RelacionamentoUpdateRequest request = new RelacionamentoUpdateRequest(StatusRelacionamento.FRIO, null, null);

        Optional<RelacionamentoResponse> result = service.update(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(StatusRelacionamento.FRIO);
        verify(repository).save(entity);
        assertThat(entity.getStatus()).isEqualTo(StatusRelacionamento.FRIO);
    }

    @Test
    void delete_shouldReturnFalseWhenNotExists() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThat(service.delete(999L)).isFalse();
    }

    @Test
    void delete_shouldDeleteAndReturnTrueWhenExists() {
        Relacionamento entity = relacionamento(1L, CanalRelacionamento.EMAIL, StatusRelacionamento.QUENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertThat(service.delete(1L)).isTrue();
        verify(repository).deleteById(1L);
    }

    private static Relacionamento relacionamento(Long id, CanalRelacionamento canal, StatusRelacionamento status) {
        Relacionamento r = new Relacionamento();
        r.setId(id);
        r.setNome("Foo");
        r.setEmail("foo@x.com");
        r.setTelefone(null);
        r.setCanal(canal);
        r.setStatus(status);
        r.setDataUltimaInteracao(null);
        r.setTipoInteracao(TipoInteracao.MOTIVADA_PELO_CLIENTE);
        r.setCliente(null);
        r.setCreatedAt(Instant.now());
        return r;
    }
}
