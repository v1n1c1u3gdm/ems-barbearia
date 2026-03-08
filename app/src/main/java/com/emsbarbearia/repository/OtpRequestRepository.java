package com.emsbarbearia.repository;

import com.emsbarbearia.entity.OtpRequest;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findFirstByPhoneOrderByCreatedAtDesc(String phone);

    @Query("SELECT o FROM OtpRequest o WHERE o.phone = :phone AND o.code = :code AND o.expiresAt > :now")
    Optional<OtpRequest> findValidByPhoneAndCode(String phone, String code, Instant now);

    @Modifying
    @Query("DELETE FROM OtpRequest o WHERE o.phone = :phone")
    void deleteByPhone(String phone);
}
