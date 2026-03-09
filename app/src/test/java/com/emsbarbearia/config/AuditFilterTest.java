package com.emsbarbearia.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.emsbarbearia.service.AuditLogService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuditFilterTest {

    @Mock
    AuditLogService auditLogService;

    @Mock
    FilterChain filterChain;

    @Test
    void doFilter_shouldLogWhenGetRequest() throws Exception {
        AuditFilter filter = new AuditFilter(auditLogService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/servicos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(auditLogService).log(eq("GET /servicos"), eq(null), eq(null), eq("GET"), eq("/servicos"), eq(200));
    }

    @Test
    void doFilter_shouldNotLogWhenPostRequest() throws Exception {
        AuditFilter filter = new AuditFilter(auditLogService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/agendamentos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(auditLogService, never()).log(eq("POST /admin/agendamentos"), eq(null), eq(null), eq("POST"), eq("/admin/agendamentos"), eq(200));
    }

    @Test
    void doFilter_shouldNotLogWhenPathIsActuator() throws Exception {
        AuditFilter filter = new AuditFilter(auditLogService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(auditLogService, never()).log(eq("GET /actuator/health"), eq(null), eq(null), eq("GET"), eq("/actuator/health"), eq(200));
    }
}
