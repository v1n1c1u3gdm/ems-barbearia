package com.emsbarbearia.repository;

import com.emsbarbearia.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByNomeContainingIgnoreCase(String nome);
    List<Staff> findByAtivoTrue();
}
