package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.port.out.BankingProviderPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/v1/banking")
@ConditionalOnProperty(name = "enable-banking.enabled", havingValue = "true")
public class BankingController {
    private final BankingService service;
    public BankingController(BankingService service) { this.service = service; }
    public record ConnectRequest(@NotBlank @Size(max = 255) String bankName,
        @NotNull @Pattern(regexp = "[A-Z]{2}") String country,
        @NotNull @Pattern(regexp = "personal|business") String psuType) {}
    public record CompleteRequest(@NotBlank @Size(max = 200) String state,
        @Size(max = 4096) String code, @Size(max = 255) String error) {}

    @GetMapping("/banks")
    public List<BankingProviderPort.Bank> banks(@RequestParam @Pattern(regexp = "[A-Z]{2}") String country) {
        return service.banks(country);
    }
    @PostMapping("/connections")
    public BankingService.Started connect(@AuthenticationPrincipal UUID userId, @Valid @RequestBody ConnectRequest body) {
        return service.start(userId, body.bankName(), body.country(), body.psuType());
    }
    @PostMapping("/connections/complete")
    public BankingService.ConnectionView complete(@AuthenticationPrincipal UUID userId, @Valid @RequestBody CompleteRequest body) {
        boolean hasCode = body.code() != null && !body.code().isBlank();
        boolean hasError = body.error() != null && !body.error().isBlank();
        if (hasCode == hasError) throw new IllegalArgumentException("Supply either code or error");
        return service.complete(userId, body.state(), body.code(), hasError ? body.error() : null);
    }
    @GetMapping("/connections")
    public List<BankingService.ConnectionView> list(@AuthenticationPrincipal UUID userId) { return service.list(userId); }
    @PostMapping("/connections/{id}/sync")
    public BankingService.ConnectionView sync(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) { return service.sync(userId, id); }
    @DeleteMapping("/connections/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void disconnect(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) { service.disconnect(userId, id); }
}
