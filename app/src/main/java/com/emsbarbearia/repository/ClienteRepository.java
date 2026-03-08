package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT MAX(c.createdAt) FROM Cliente c")
    Optional<Instant> findLatestCreatedAt();
}
