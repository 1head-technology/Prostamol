package com.prostamol.Prostamol.infrastructure.security.passkey;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class PasskeyConfig {
    @Bean
    public RelyingParty relyingParty(WebAuthnCredentialRepository repository,
            @Value("${passkey.rp-id}") String rpId,
            @Value("${passkey.rp-name}") String rpName,
            @Value("${passkey.origins}") String origins) {
        return RelyingParty.builder()
            .identity(RelyingPartyIdentity.builder().id(rpId).name(rpName).build())
            .credentialRepository(repository)
            .origins(Arrays.stream(origins.split(",")).map(String::trim).collect(Collectors.toSet()))
            .allowOriginPort(false).allowOriginSubdomain(false)
            .build();
    }
}
