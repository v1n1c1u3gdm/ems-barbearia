package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.config.PublicClienteAuthentication;
import com.emsbarbearia.entity.AuditLog;
import com.emsbarbearia.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    AuditLogRepository repository;

    AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(repository, new ObjectMapper());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveSolicitante_shouldReturnPublicoWhenNoAuthentication() {
        String result = service.resolveSolicitante();
        assertThat(result).isEqualTo("publico");
    }

    @Test
    void resolveSolicitante_shouldReturnClienteIdWhenPublicClienteAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new PublicClienteAuthentication(42L));

        String result = service.resolveSolicitante();

        assertThat(result).isEqualTo("cliente:42");
    }

    @Test
    void log_shouldNotThrowWhenRepositoryThrows() {
        when(repository.save(any(AuditLog.class))).thenThrow(new RuntimeException("db error"));

        service.log("GET /servicos", null, null);
    }

    @Test
    void doLog_shouldPersistWithSolicitanteAndAcao() {
        service.doLog("GET /servicos", null, null, "GET", "/servicos", 200);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getAcao()).isEqualTo("GET /servicos");
        assertThat(saved.getSolicitante()).isEqualTo("publico");
        assertThat(saved.getEstadoAnterior()).isNull();
        assertThat(saved.getEstadoPosterior()).isNull();
        assertThat(saved.getMetodoHttp()).isEqualTo("GET");
        assertThat(saved.getPath()).isEqualTo("/servicos");
        assertThat(saved.getStatusHttp()).isEqualTo(200);
    }

    @Test
    void doLog_shouldSerializeEstadoWhenProvided() {
        Object estado = Map.of("id", 1L, "nome", "Test");

        service.doLog("PUT /admin/clientes/1", null, estado, null, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getEstadoPosterior()).contains("1");
        assertThat(saved.getEstadoPosterior()).contains("Test");
    }
}
