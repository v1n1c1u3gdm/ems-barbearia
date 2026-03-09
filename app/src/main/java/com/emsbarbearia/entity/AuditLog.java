package com.emsbarbearia.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    private static final int MAX_ESTADO_LENGTH = 65_536;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private Instant dataHora;

    @Column(name = "acao", nullable = false, length = 500)
    private String acao;

    @Column(name = "estado_anterior", columnDefinition = "TEXT")
    private String estadoAnterior;

    @Column(name = "estado_posterior", columnDefinition = "TEXT")
    private String estadoPosterior;

    @Column(name = "solicitante", nullable = false, length = 255)
    private String solicitante;

    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "status_http")
    private Integer statusHttp;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public void setDataHora(Instant dataHora) {
        this.dataHora = dataHora;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = truncate(estadoAnterior, MAX_ESTADO_LENGTH);
    }

    public String getEstadoPosterior() {
        return estadoPosterior;
    }

    public void setEstadoPosterior(String estadoPosterior) {
        this.estadoPosterior = truncate(estadoPosterior, MAX_ESTADO_LENGTH);
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatusHttp() {
        return statusHttp;
    }

    public void setStatusHttp(Integer statusHttp) {
        this.statusHttp = statusHttp;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }
}
