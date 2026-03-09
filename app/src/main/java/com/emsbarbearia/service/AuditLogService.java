package com.emsbarbearia.service;

import com.emsbarbearia.config.PublicClienteAuthentication;
import com.emsbarbearia.entity.AuditLog;
import com.emsbarbearia.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final int MAX_ESTADO_LENGTH = 65_536;
    private static final String SOLICITANTE_PUBLICO = "publico";

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper.copy()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public void log(String acao, Object estadoAnterior, Object estadoPosterior) {
        log(acao, estadoAnterior, estadoPosterior, null, null, null);
    }

    public void log(String acao, Object estadoAnterior, Object estadoPosterior,
                    String metodoHttp, String path, Integer statusHttp) {
        try {
            doLog(acao, estadoAnterior, estadoPosterior, metodoHttp, path, statusHttp);
        } catch (Exception ignored) {
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doLog(String acao, Object estadoAnterior, Object estadoPosterior,
                      String metodoHttp, String path, Integer statusHttp) {
        AuditLog log = new AuditLog();
        log.setAcao(acao != null && acao.length() > 500 ? acao.substring(0, 500) : acao);
        log.setSolicitante(resolveSolicitante());
        log.setEstadoAnterior(serialize(estadoAnterior));
        log.setEstadoPosterior(serialize(estadoPosterior));
        log.setMetodoHttp(metodoHttp != null && metodoHttp.length() > 10 ? metodoHttp.substring(0, 10) : metodoHttp);
        log.setPath(path != null && path.length() > 500 ? path.substring(0, 500) : path);
        log.setStatusHttp(statusHttp);
        repository.save(log);
    }

    public String resolveSolicitante() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return SOLICITANTE_PUBLICO;
        }
        if (auth instanceof PublicClienteAuthentication publicAuth) {
            return "cliente:" + publicAuth.getClienteId();
        }
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            return SOLICITANTE_PUBLICO;
        }
        return "admin:" + name;
    }

    private String serialize(Object value) {
        if (value == null) return null;
        try {
            String json = objectMapper.writeValueAsString(value);
            return json.length() > MAX_ESTADO_LENGTH ? json.substring(0, MAX_ESTADO_LENGTH) : json;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
