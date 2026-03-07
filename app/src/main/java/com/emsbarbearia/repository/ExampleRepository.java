package com.emsbarbearia.repository;

import com.emsbarbearia.entity.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleRepository extends JpaRepository<ExampleEntity, Long> {

    List<ExampleEntity> findByNameContainingIgnoreCase(String name);
}
