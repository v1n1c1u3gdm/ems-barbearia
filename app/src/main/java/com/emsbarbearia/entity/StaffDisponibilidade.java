package com.emsbarbearia.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Table(name = "staff_disponibilidade")
@IdClass(StaffDisponibilidade.StaffDisponibilidadeId.class)
public class StaffDisponibilidade {

    @Id
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Id
    @Column(name = "dia_semana", nullable = false, columnDefinition = "TINYINT")
    private Integer diaSemana;

    @Column(nullable = false)
    private Boolean aberto = false;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public Boolean getAberto() { return aberto; }
    public void setAberto(Boolean aberto) { this.aberto = aberto; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public static class StaffDisponibilidadeId implements Serializable {
        private Long staffId;
        private Integer diaSemana;
        public Long getStaffId() { return staffId; }
        public void setStaffId(Long staffId) { this.staffId = staffId; }
        public Integer getDiaSemana() { return diaSemana; }
        public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StaffDisponibilidadeId that = (StaffDisponibilidadeId) o;
            return java.util.Objects.equals(staffId, that.staffId) && java.util.Objects.equals(diaSemana, that.diaSemana);
        }
        @Override
        public int hashCode() { return java.util.Objects.hash(staffId, diaSemana); }
    }
}
