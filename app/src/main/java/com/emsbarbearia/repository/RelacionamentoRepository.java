package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Relacionamento;
import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RelacionamentoRepository extends JpaRepository<Relacionamento, Long> {

    List<Relacionamento> findByCanal(CanalRelacionamento canal);

    List<Relacionamento> findByCanalAndStatus(CanalRelacionamento canal, StatusRelacionamento status);

    List<Relacionamento> findByClienteId(Long clienteId);

    List<Relacionamento> findByStatus(StatusRelacionamento status);

    List<Relacionamento> findAllByOrderByCreatedAtDesc();

    @Query("SELECT MAX(r.createdAt) FROM Relacionamento r")
    Optional<Instant> findLatestCreatedAt();
}
