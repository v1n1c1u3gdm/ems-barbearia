package com.emsbarbearia.repository;

import com.emsbarbearia.entity.ClienteCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteCredentialRepository extends JpaRepository<ClienteCredential, Long> {

    Optional<ClienteCredential> findByProviderAndExternalId(String provider, String externalId);
}
