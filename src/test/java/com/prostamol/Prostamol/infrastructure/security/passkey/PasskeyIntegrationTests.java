package com.prostamol.Prostamol.infrastructure.security.passkey;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.prostamol.Prostamol.domain.model.user.*;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;
import com.prostamol.Prostamol.infrastructure.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:passkeys;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "passkey.rp-id=localhost",
    "passkey.origins=http://localhost:3000"
})
@AutoConfigureMockMvc
class PasskeyIntegrationTests {
    @Autowired PasskeyService service;
    @Autowired PasskeyChallengeStore challenges;
    @Autowired PasskeyChallengeRepository challengeRepository;
    @Autowired PasskeyCredentialRepository credentials;
    @Autowired UserRepositoryPort users;
    @Autowired JwtService jwt;
    @Autowired MockMvc mvc;
    final JsonMapper json = JsonMapper.builder().build();
    User user;
    KeyPair key;
    byte[] credentialId;

    @BeforeEach
    void setup() throws Exception {
        user = users.save(new User(UUID.randomUUID(), UUID.randomUUID() + "@example.com",
            "private-password-hash", "Test User", "EUR", Role.USER));
        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        key = generator.generateKeyPair();
        credentialId = new byte[32];
        new SecureRandom().nextBytes(credentialId);
    }

    @Test
    void enrollmentAndLoginReturnJwtWithoutSecretsAndUpdateCounter() throws Exception {
        var options = service.startRegistration(user.getId());
        assertEquals("required", options.publicKey().at("/authenticatorSelection/residentKey").asText());
        assertEquals("required", options.publicKey().at("/authenticatorSelection/userVerification").asText());
        mvc.perform(post("/api/v1/passkeys/register/verify")
            .header("Authorization", bearer()).contentType("application/json")
            .content(json.writeValueAsString(Map.of("requestId", options.requestId(), "name", "My phone",
                "credential", json.readTree(registration(options, "http://localhost:3000", 0x45))))))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("My phone"));
        var login = service.startLogin();
        assertEquals("required", login.publicKey().get("userVerification").asText());
        var result = mvc.perform(post("/api/v1/auth/passkeys/verify").contentType("application/json")
            .content(json.writeValueAsString(Map.of("requestId", login.requestId(), "credential",
                json.readTree(assertion(login, "http://localhost:3000", 5, 1))))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.user.id").value(user.getId().toString()))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist()).andReturn();
        assertTrue(jwt.isValid(json.readTree(result.getResponse().getContentAsString()).get("token").asText()));
        var saved = credentials.findByCredentialId(b64(credentialId)).orElseThrow();
        assertEquals(1, saved.getSignatureCount());
        assertNotNull(saved.getLastUsedAt());
        assertThrows(PasskeyException.class, () -> service.finishLogin(login.requestId(), assertion(login, "http://localhost:3000", 5, 2)));
    }

    @Test
    void invalidOriginAndMissingUserVerificationRejectEnrollment() throws Exception {
        for (int flags : new int[]{0x41, 0x45}) {
            var options = service.startRegistration(user.getId());
            String origin = flags == 0x45 ? "https://attacker.example" : "http://localhost:3000";
            assertThrows(PasskeyException.class, () -> service.finishRegistration(user.getId(), options.requestId(),
                "Phone", registration(options, origin, flags)));
            assertFalse(challengeRepository.existsById(options.requestId()));
        }
        assertTrue(service.list(user.getId()).isEmpty());
    }

    @Test
    void rejectsWrongSignatureChallengeOriginVerificationAndStaleCounter() throws Exception {
        enroll();
        for (String failure : List.of("signature", "challenge", "origin", "uv")) {
            var options = service.startLogin();
            var signingOptions = failure.equals("challenge") ? service.startLogin() : options;
            String response = assertion(signingOptions,
                failure.equals("origin") ? "https://attacker.example" : "http://localhost:3000",
                failure.equals("uv") ? 1 : 5, 1);
            if (failure.equals("signature")) {
                var node = (tools.jackson.databind.node.ObjectNode) json.readTree(response);
                ((tools.jackson.databind.node.ObjectNode) node.get("response")).put("signature", b64(new byte[64]));
                response = node.toString();
            }
            String finalResponse = response;
            assertThrows(PasskeyException.class, () -> service.finishLogin(options.requestId(), finalResponse), failure);
            assertFalse(challengeRepository.existsById(options.requestId()));
        }
        var first = service.startLogin();
        service.finishLogin(first.requestId(), assertion(first, "http://localhost:3000", 5, 2));
        var stale = service.startLogin();
        assertThrows(PasskeyException.class, () -> service.finishLogin(stale.requestId(), assertion(stale, "http://localhost:3000", 5, 1)));
    }

    @Test
    void syncedCredentialsWithZeroCountersCanSignInRepeatedly() throws Exception {
        enroll();
        for (int i = 0; i < 2; i++) {
            var options = service.startLogin();
            assertEquals(user.getId(), service.finishLogin(options.requestId(),
                assertion(options, "http://localhost:3000", 5, 0)).getId());
        }
    }

    @Test
    void wrongUserHandleIsRejected() throws Exception {
        enroll();
        var options = service.startLogin();
        var response = (tools.jackson.databind.node.ObjectNode) json.readTree(assertion(options, "http://localhost:3000", 5, 1));
        ((tools.jackson.databind.node.ObjectNode) response.get("response"))
            .put("userHandle", WebAuthnCredentialRepository.userHandle(UUID.randomUUID()).getBase64Url());
        assertThrows(PasskeyException.class, () -> service.finishLogin(options.requestId(), response.toString()));
    }

    @Test
    void enrollmentIsBoundToUserAndCannotBeReplayed() throws Exception {
        var options = service.startRegistration(user.getId());
        String response = registration(options, "http://localhost:3000", 0x45);
        assertThrows(PasskeyException.class, () -> service.finishRegistration(UUID.randomUUID(), options.requestId(), "Phone", response));
        service.finishRegistration(user.getId(), options.requestId(), "Phone", response);
        assertThrows(PasskeyException.class, () -> service.finishRegistration(user.getId(), options.requestId(), "Phone", response));
        var duplicate = service.startRegistration(user.getId());
        assertEquals(1, duplicate.publicKey().get("excludeCredentials").size());
        assertThrows(PasskeyException.class, () -> service.finishRegistration(user.getId(), duplicate.requestId(), "Duplicate",
            registration(duplicate, "http://localhost:3000", 0x45)));
    }

    @Test
    void expiredAndWrongCeremonyChallengesAreRejected() throws Exception {
        var expired = challengeRepository.saveAndFlush(new PasskeyChallenge("authentication", null, "{}", Instant.now().minusSeconds(1)));
        assertThrows(PasskeyException.class, () -> challenges.consume(expired.getId(), "authentication", null));
        var options = service.startRegistration(user.getId());
        assertThrows(PasskeyException.class, () -> service.finishLogin(options.requestId(), "{}"));
        assertFalse(challengeRepository.existsById(expired.getId())); // cleaned on next start
    }

    @Test
    void challengeHasExactlyOneWinnerAcrossConcurrentRequests() throws Exception {
        UUID id = challenges.create("authentication", null, "{}");
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            Callable<Boolean> consume = () -> {
                start.await();
                try { challenges.consume(id, "authentication", null); return true; }
                catch (PasskeyException e) { return false; }
            };
            var first = executor.submit(consume);
            var second = executor.submit(consume);
            start.countDown();
            assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void managementRequiresJwtAndIsScopedToOwnerAndDeletionRevokesLogin() throws Exception {
        var saved = enroll();
        mvc.perform(post("/api/v1/passkeys/register/options")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/passkeys/register/verify").contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/passkeys")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/passkeys/" + saved.id())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/passkeys/options")).andExpect(status().isOk());
        String other = "Bearer " + jwt.generateToken(UUID.randomUUID(), "other@example.com", Role.USER);
        mvc.perform(get("/api/v1/passkeys").header("Authorization", other))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(delete("/api/v1/passkeys/" + saved.id()).header("Authorization", other)).andExpect(status().isNoContent());
        assertEquals(1, service.list(user.getId()).size());
        var options = service.startLogin();
        mvc.perform(delete("/api/v1/passkeys/" + saved.id()).header("Authorization", bearer())).andExpect(status().isNoContent());
        assertThrows(PasskeyException.class, () -> service.finishLogin(options.requestId(), assertion(options, "http://localhost:3000", 5, 1)));
    }

    @Test
    void malformedResponseConsumesChallengeAndReturnsGenericUnauthorized() throws Exception {
        var options = service.startLogin();
        mvc.perform(post("/api/v1/auth/passkeys/verify").contentType("application/json")
            .content(json.writeValueAsString(Map.of("requestId", options.requestId(), "credential", Map.of()))))
            .andExpect(status().isUnauthorized());
        assertFalse(challengeRepository.existsById(options.requestId()));
    }

    PasskeyService.Summary enroll() throws Exception {
        var options = service.startRegistration(user.getId());
        return service.finishRegistration(user.getId(), options.requestId(), "Phone", registration(options, "http://localhost:3000", 0x45));
    }
    String bearer() { return "Bearer " + jwt.generateToken(user.getId(), user.getEmail(), user.getRole()); }
    String registration(PasskeyService.Options options, String origin, int flags) throws Exception {
        var publicKey = (ECPublicKey) key.getPublic();
        var cose = new ByteArrayOutputStream();
        try (var cbor = new CBORFactory().createGenerator(cose)) {
            cbor.writeStartObject();
            cbor.writeFieldId(1); cbor.writeNumber(2);
            cbor.writeFieldId(3); cbor.writeNumber(-7);
            cbor.writeFieldId(-1); cbor.writeNumber(1);
            cbor.writeFieldId(-2); cbor.writeBinary(coordinate(publicKey.getW().getAffineX().toByteArray()));
            cbor.writeFieldId(-3); cbor.writeBinary(coordinate(publicKey.getW().getAffineY().toByteArray()));
            cbor.writeEndObject();
        }
        var authData = new ByteArrayOutputStream();
        authData.write(authData(flags, 0));
        authData.write(new byte[16]);
        authData.write(ByteBuffer.allocate(2).putShort((short) credentialId.length).array());
        authData.write(credentialId);
        authData.write(cose.toByteArray());
        var attestation = new ByteArrayOutputStream();
        try (var cbor = new CBORFactory().createGenerator(attestation)) {
            cbor.writeStartObject();
            cbor.writeStringField("fmt", "none");
            cbor.writeFieldName("attStmt"); cbor.writeStartObject(); cbor.writeEndObject();
            cbor.writeBinaryField("authData", authData.toByteArray()); cbor.writeEndObject();
        }
        return response(Map.of("clientDataJSON", b64(clientData(options, origin, "webauthn.create")),
            "attestationObject", b64(attestation.toByteArray()), "transports", List.of("internal")));
    }
    String assertion(PasskeyService.Options options, String origin, int flags, int count) throws Exception {
        byte[] clientData = clientData(options, origin, "webauthn.get");
        byte[] authData = authData(flags, count);
        var signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(key.getPrivate()); signer.update(authData); signer.update(hash(clientData));
        return response(Map.of("clientDataJSON", b64(clientData), "authenticatorData", b64(authData),
            "signature", b64(signer.sign()), "userHandle", WebAuthnCredentialRepository.userHandle(user.getId()).getBase64Url()));
    }
    String response(Map<String, Object> response) {
        return json.writeValueAsString(Map.of("id", b64(credentialId), "rawId", b64(credentialId),
            "type", "public-key", "response", response, "clientExtensionResults", Map.of()));
    }
    byte[] clientData(PasskeyService.Options options, String origin, String type) {
        return json.writeValueAsBytes(Map.of("type", type, "challenge", options.publicKey().get("challenge").asText(), "origin", origin));
    }
    byte[] authData(int flags, int count) throws Exception {
        return ByteBuffer.allocate(37).put(hash("localhost".getBytes(StandardCharsets.UTF_8))).put((byte) flags).putInt(count).array();
    }
    static byte[] coordinate(byte[] value) {
        byte[] result = new byte[32];
        int length = Math.min(value.length, 32);
        System.arraycopy(value, value.length - length, result, 32 - length, length);
        return result;
    }
    static byte[] hash(byte[] value) throws Exception { return MessageDigest.getInstance("SHA-256").digest(value); }
    static String b64(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
}
