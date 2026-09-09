package com.prostamol.Prostamol.infrastructure.security.passkey;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, UUID> {
    List<PasskeyCredential> findAllByUserIdOrderByCreatedAtAsc(UUID userId);
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    Optional<PasskeyCredential> findByIdAndUserId(UUID id, UUID userId);
}
