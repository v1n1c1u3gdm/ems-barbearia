package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {

    List<Assinatura> findByClienteId(Long clienteId);
}
