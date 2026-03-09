package com.emsbarbearia.config;

import com.emsbarbearia.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuditFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATH_PREFIXES = List.of(
        "/actuator/",
        "/v3/api-docs",
        "/swagger-ui"
    );

    private final AuditLogService auditLogService;

    public AuditFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (shouldLog(request)) {
            String method = request.getMethod();
            String path = request.getRequestURI();
            String acao = method + " " + path;
            auditLogService.log(acao, null, null, method, path, response.getStatus());
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String method = request.getMethod();
        if (!("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method))) {
            return false;
        }
        String path = request.getRequestURI();
        for (String prefix : EXCLUDED_PATH_PREFIXES) {
            if (path != null && path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
}
