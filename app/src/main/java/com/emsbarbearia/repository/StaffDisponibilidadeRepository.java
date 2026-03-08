package com.emsbarbearia.repository;

import com.emsbarbearia.entity.StaffDisponibilidade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffDisponibilidadeRepository extends JpaRepository<StaffDisponibilidade, StaffDisponibilidade.StaffDisponibilidadeId> {

    List<StaffDisponibilidade> findByStaffIdOrderByDiaSemanaAsc(Long staffId);

    List<StaffDisponibilidade> findByStaffIdInOrderByStaffIdAscDiaSemanaAsc(List<Long> staffIds);

    @Modifying
    @Query("DELETE FROM StaffDisponibilidade d WHERE d.staffId = :staffId")
    void deleteByStaffId(@Param("staffId") Long staffId);
}
