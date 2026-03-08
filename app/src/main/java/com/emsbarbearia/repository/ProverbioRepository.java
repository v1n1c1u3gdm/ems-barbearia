package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Proverbio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProverbioRepository extends JpaRepository<Proverbio, Long> {

    @Query(value = "SELECT * FROM proverbio ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Proverbio> findRandom();
}
