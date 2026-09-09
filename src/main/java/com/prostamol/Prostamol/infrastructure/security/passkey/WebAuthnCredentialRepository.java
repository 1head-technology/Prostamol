package com.prostamol.Prostamol.infrastructure.security.passkey;

import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.*;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebAuthnCredentialRepository implements CredentialRepository {
    private final PasskeyCredentialRepository credentials;
    private final UserRepositoryPort users;
    public WebAuthnCredentialRepository(PasskeyCredentialRepository credentials, UserRepositoryPort users) {
        this.credentials = credentials;
        this.users = users;
    }
    static ByteArray userHandle(UUID id) {
        return new ByteArray(id.toString().getBytes(StandardCharsets.UTF_8));
    }
    static ByteArray decode(String value) {
        try { return ByteArray.fromBase64Url(value); }
        catch (Exception e) { throw new IllegalStateException("Invalid stored passkey encoding", e); }
    }
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return users.findByEmail(username).map(user -> credentials.findAllByUserIdOrderByCreatedAtAsc(user.getId())
            .stream().map(c -> PublicKeyCredentialDescriptor.builder().id(decode(c.getCredentialId())).build())
            .collect(Collectors.toSet())).orElseGet(Set::of);
    }
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return users.findByEmail(username).map(user -> userHandle(user.getId()));
    }
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray handle) {
        try {
            UUID id = UUID.fromString(new String(handle.getBytes(), StandardCharsets.UTF_8));
            if (!userHandle(id).equals(handle)) { return Optional.empty(); }
            return users.findById(id).map(user -> user.getEmail());
        } catch (IllegalArgumentException e) { return Optional.empty(); }
    }
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentials.findByCredentialId(credentialId.getBase64Url())
            .filter(c -> userHandle(c.getUserId()).equals(userHandle)).map(this::registered);
    }
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentials.findByCredentialId(credentialId.getBase64Url())
            .map(c -> Set.of(registered(c))).orElseGet(Set::of);
    }
    private RegisteredCredential registered(PasskeyCredential credential) {
        return RegisteredCredential.builder().credentialId(decode(credential.getCredentialId()))
            .userHandle(userHandle(credential.getUserId())).publicKeyCose(decode(credential.getPublicKeyCose()))
            .signatureCount(credential.getSignatureCount()).build();
    }
}
