package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByNomeContainingIgnoreCase(String nome);
}
