package com.prostamol.Prostamol.infrastructure.security.passkey;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
public class PasskeyChallengeStore {
    private final PasskeyChallengeRepository repository;
    public PasskeyChallengeStore(PasskeyChallengeRepository repository) { this.repository = repository; }

    @Transactional
    public UUID create(String ceremony, UUID userId, String optionsJson) {
        Instant now = Instant.now();
        repository.deleteExpired(now);
        return repository.save(new PasskeyChallenge(ceremony, userId, optionsJson, now.plusSeconds(300))).getId();
    }

    // Commit consumption even when verification subsequently fails. The delete's row count
    // ensures only one request wins, including across multiple application instances.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String consume(UUID id, String ceremony, UUID userId) {
        PasskeyChallenge challenge = repository.findById(id).orElseThrow(PasskeyException::new);
        if (!challenge.getCeremony().equals(ceremony) || !Objects.equals(challenge.getUserId(), userId)
                || !challenge.getExpiresAt().isAfter(Instant.now())) {
            throw new PasskeyException();
        }
        if (repository.consume(id) != 1) { throw new PasskeyException(); }
        return challenge.getOptionsJson();
    }
}
