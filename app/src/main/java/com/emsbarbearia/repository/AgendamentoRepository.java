package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByClienteId(Long clienteId);

    @Query("SELECT a FROM Agendamento a WHERE a.staff.id = :staffId AND a.tipo = 'FIRME' AND a.dataHora < :end AND (a.dataHoraFim IS NULL OR a.dataHoraFim > :start)")
    List<Agendamento> findOverlappingFirmeByStaff(Long staffId, Instant start, Instant end);

    List<Agendamento> findByStaffId(Long staffId);

    List<Agendamento> findByDataHoraBetweenOrderByDataHora(Instant start, Instant end);

    @Query("SELECT MAX(a.createdAt) FROM Agendamento a")
    Optional<Instant> findLatestCreatedAt();
}
