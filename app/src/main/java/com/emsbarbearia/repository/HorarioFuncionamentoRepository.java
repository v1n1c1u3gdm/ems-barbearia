package com.emsbarbearia.repository;

import com.emsbarbearia.entity.HorarioFuncionamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, Integer> {

    List<HorarioFuncionamento> findAllByOrderByDiaSemanaAsc();
}
