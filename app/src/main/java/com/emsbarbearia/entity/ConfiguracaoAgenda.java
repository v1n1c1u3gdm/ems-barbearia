package com.emsbarbearia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracao_agenda")
public class ConfiguracaoAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_minutos", nullable = false)
    private Integer slotMinutos = 30;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getSlotMinutos() { return slotMinutos; }
    public void setSlotMinutos(Integer slotMinutos) { this.slotMinutos = slotMinutos; }
}
