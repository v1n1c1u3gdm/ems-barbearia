package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByTituloContainingIgnoreCase(String titulo);
    List<Servico> findByAtivoTrue();
    @Query("SELECT MAX(s.createdAt) FROM Servico s")
    Optional<Instant> findLatestCreatedAt();
}
