package com.emsbarbearia.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horario_funcionamento")
public class HorarioFuncionamento {

    @Id
    @Column(name = "dia_semana", nullable = false, columnDefinition = "TINYINT")
    private Integer diaSemana;

    @Column(nullable = false)
    private Boolean aberto = false;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public Boolean getAberto() { return aberto; }
    public void setAberto(Boolean aberto) { this.aberto = aberto; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
}
