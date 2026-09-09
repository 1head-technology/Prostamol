package com.prostamol.Prostamol.infrastructure.security.passkey;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.UUID;

public interface PasskeyChallengeRepository extends JpaRepository<PasskeyChallenge, UUID> {
    @Modifying
    @Query("delete from PasskeyChallenge c where c.id = :id")
    int consume(@Param("id") UUID id);

    @Modifying
    @Query("delete from PasskeyChallenge c where c.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
