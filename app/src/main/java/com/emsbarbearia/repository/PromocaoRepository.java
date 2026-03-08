package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Promocao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    List<Promocao> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT MAX(p.createdAt) FROM Promocao p")
    Optional<Instant> findLatestCreatedAt();
}
