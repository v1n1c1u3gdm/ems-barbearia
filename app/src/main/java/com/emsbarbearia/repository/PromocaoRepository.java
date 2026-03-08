package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Promocao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    List<Promocao> findByTituloContainingIgnoreCase(String titulo);
}
