package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT MAX(c.createdAt) FROM Contato c")
    Optional<Instant> findLatestCreatedAt();
}
