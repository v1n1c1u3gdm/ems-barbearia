package com.emsbarbearia.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "relacionamento")
public class Relacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalRelacionamento canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRelacionamento status;

    @Column(name = "data_ultima_interacao")
    private Instant dataUltimaInteracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_interacao", nullable = false, length = 30)
    private TipoInteracao tipoInteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public CanalRelacionamento getCanal() {
        return canal;
    }

    public void setCanal(CanalRelacionamento canal) {
        this.canal = canal;
    }

    public StatusRelacionamento getStatus() {
        return status;
    }

    public void setStatus(StatusRelacionamento status) {
        this.status = status;
    }

    public Instant getDataUltimaInteracao() {
        return dataUltimaInteracao;
    }

    public void setDataUltimaInteracao(Instant dataUltimaInteracao) {
        this.dataUltimaInteracao = dataUltimaInteracao;
    }

    public TipoInteracao getTipoInteracao() {
        return tipoInteracao;
    }

    public void setTipoInteracao(TipoInteracao tipoInteracao) {
        this.tipoInteracao = tipoInteracao;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
