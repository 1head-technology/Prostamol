package com.prostamol.Prostamol.infrastructure.security.passkey;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class PasskeyService {
    private final RelyingParty relyingParty;
    private final PasskeyChallengeStore challenges;
    private final PasskeyCredentialRepository credentials;
    private final UserRepositoryPort users;
    private final JsonMapper json = JsonMapper.builder().build();

    public PasskeyService(
        RelyingParty relyingParty,
        PasskeyChallengeStore challenges,
        PasskeyCredentialRepository credentials,
        UserRepositoryPort users
    ) {
        this.relyingParty = relyingParty;
        this.challenges = challenges;
        this.credentials = credentials;
        this.users = users;
    }

    public record Options(UUID requestId, JsonNode publicKey) {}

    public record Summary(UUID id, String name, Instant createdAt, Instant lastUsedAt) {
        static Summary of(PasskeyCredential c) {
            return new Summary(c.getId(), c.getName(), c.getCreatedAt(), c.getLastUsedAt());
        }
    }

    public Options startRegistration(UUID userId) throws IOException {
        User user = users.findById(userId).orElseThrow(PasskeyException::new);

        var request = relyingParty.startRegistration(StartRegistrationOptions.builder()
            .user(UserIdentity.builder().name(user.getEmail()).displayName(user.getName())
            .id(WebAuthnCredentialRepository.userHandle(userId)).build())
            .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
            .residentKey(ResidentKeyRequirement.REQUIRED)
            .userVerification(UserVerificationRequirement.REQUIRED).build())
            .timeout(300_000).build());

        UUID id = challenges.create("registration", userId, request.toJson());

        return new Options(id, json.readTree(request.toCredentialsCreateJson()).get("publicKey"));
    }

    @Transactional
    public Summary finishRegistration(UUID userId, UUID requestId, String name, String responseJson) {
        String stored = challenges.consume(requestId, "registration", userId);
        users.findById(userId).orElseThrow(PasskeyException::new);

        var result = verifyRegistration(stored, responseJson);
        var credential = new PasskeyCredential(
            result.getKeyId().getId().getBase64Url(),
            userId,
            result.getPublicKeyCose().getBase64Url(),
            result.getSignatureCount(),
            name.trim()
        );

        try {
            return Summary.of(credentials.saveAndFlush(credential));
        }
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new PasskeyException();
        }
    }

    private RegistrationResult verifyRegistration(String stored, String responseJson) {
        try {
            return relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                .request(PublicKeyCredentialCreationOptions.fromJson(stored))
                .response(PublicKeyCredential.parseRegistrationResponseJson(responseJson)).build());
        }
        catch (IOException | RegistrationFailedException | RuntimeException e) {
            throw new PasskeyException();
        }
    }

    public Options startLogin() throws IOException {
        var request = relyingParty
            .startAssertion(StartAssertionOptions.builder()
            .userVerification(UserVerificationRequirement.REQUIRED).timeout(300_000).build());

        UUID id = challenges.create("authentication", null, request.toJson());

        return new Options(id, json.readTree(request.toCredentialsGetJson()).get("publicKey"));
    }

    @Transactional
    public User finishLogin(UUID requestId, String responseJson) {
        String stored = challenges.consume(requestId, "authentication", null);
        var result = verifyAssertion(stored, responseJson);

        if (!result.isSuccess()) {
            throw new PasskeyException();
        }

        var credential = credentials
            .findByCredentialId(result.getCredential().getCredentialId().getBase64Url())
            .orElseThrow(PasskeyException::new);

        if (!WebAuthnCredentialRepository.userHandle(credential.getUserId()).equals(result.getCredential().getUserHandle())) {
            throw new PasskeyException();
        }

        credential.recordUse(result.getSignatureCount());
        credentials.flush(); // Optimistic version check prevents concurrent counter updates/revocation races.

        return users.findById(credential.getUserId()).orElseThrow(PasskeyException::new);
    }

    private AssertionResult verifyAssertion(String stored, String responseJson) {
        try {
            return relyingParty.finishAssertion(FinishAssertionOptions.builder()
                .request(AssertionRequest.fromJson(stored))
                .response(PublicKeyCredential.parseAssertionResponseJson(responseJson)).build());
        }
        catch (IOException | AssertionFailedException | RuntimeException e) {
            // Some malformed signatures trigger a provider RuntimeException; never expose
            // verification internals (including signed data) through the generic error handler.
            throw new PasskeyException();
        }
    }

    public List<Summary> list(UUID userId) {
        return credentials.findAllByUserIdOrderByCreatedAtAsc(userId).stream().map(Summary::of).toList();
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        credentials.findByIdAndUserId(id, userId).ifPresent(credentials::delete);
    }
}
