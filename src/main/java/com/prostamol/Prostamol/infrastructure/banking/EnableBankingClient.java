package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.out.BankingProviderPort;
import io.jsonwebtoken.Jwts;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.*;
import java.util.*;

public class EnableBankingClient implements BankingProviderPort {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final String applicationId;
    private final String redirectUrl;
    private final PrivateKey key;

    public EnableBankingClient(String applicationId, String keyPath, String redirectUrl) {
        this.applicationId = applicationId;
        this.redirectUrl = redirectUrl;
        try {
            UUID.fromString(applicationId);
            URI uri = URI.create(redirectUrl);
            if (uri.getHost() == null || uri.getFragment() != null || uri.getQuery() != null
                || !("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme()) && "localhost".equals(uri.getHost()))))
                throw new IllegalArgumentException();
            String pem = Files.readString(Path.of(keyPath)).replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        } catch (Exception ex) {
            throw new IllegalStateException("Configure Enable Banking application ID, PKCS#8 RSA private key file and redirect URL", ex);
        }
    }

    String token() {
        Instant now = Instant.now();
        return Jwts.builder().header().keyId(applicationId).type("JWT").and()
            .issuer("enablebanking.com").claim("aud", "api.enablebanking.com")
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
            .signWith(key, Jwts.SIG.RS256).compact();
    }

    private JsonNode request(String method, String path, Object body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("https://api.enablebanking.com" + path))
                .timeout(Duration.ofSeconds(45)).header("Authorization", "Bearer " + token())
                .header("Accept", "application/json");
            request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
            if (body != null) request.header("Content-Type", "application/json");
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (method.equals("DELETE") && response.statusCode() == 404) return json.createObjectNode();
                throw EnableBankingException.fromResponse(response.statusCode(), response.body());
            }
            return response.body().isBlank() ? json.createObjectNode() : json.readTree(response.body());
        } catch (BankingException ex) { throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BankingException(502, "Enable Banking request interrupted");
        } catch (Exception ex) { throw new BankingException(502, "Enable Banking unavailable or returned invalid data"); }
    }

    @Override public List<Bank> banks(String country) {
        List<Bank> banks = new ArrayList<>();
        JsonNode data = request("GET", "/aspsps?country=" + encode(country), null).path("aspsps");
        if (!data.isArray()) throw new BankingException(502, "Enable Banking response missing bank list");
        for (JsonNode b : data)
            banks.add(new Bank(required(b, "name"), required(b, "country"), b.path("maximum_consent_validity").asLong()));
        return banks;
    }
    @Override public Authorization authorize(Bank bank, String psuType, String state, String userId, Instant until) {
        return new Authorization(required(request("POST", "/auth", Map.of(
            "access", Map.of("valid_until", until.toString(), "balances", true, "transactions", true),
            "aspsp", Map.of("name", bank.name(), "country", bank.country()), "psu_type", psuType,
            "state", state, "psu_id", userId, "redirect_url", redirectUrl)), "url"));
    }
    @Override public Session exchange(String code) {
        JsonNode session = request("POST", "/sessions", Map.of("code", code));
        if (!session.path("accounts").isArray()) throw new BankingException(502, "Enable Banking response missing accounts");
        List<RemoteAccount> accounts = new ArrayList<>();
        for (JsonNode a : session.path("accounts")) {
            if (text(a, "uid") == null) continue;
            accounts.add(new RemoteAccount(required(a, "uid"), required(a, "identification_hash"),
                Optional.ofNullable(text(a, "details")).orElse(Optional.ofNullable(text(a, "product")).orElse("Bank account")),
                required(a, "currency")));
        }
        return new Session(required(session, "session_id"), Instant.parse(required(session.path("access"), "valid_until")), accounts);
    }
    @Override public Page transactions(String uid, LocalDate from, LocalDate to, String continuation) {
        String path = "/accounts/" + encode(uid) + "/transactions?transaction_status=BOOK&date_from=" + from + "&date_to=" + to;
        if (continuation != null) path += "&continuation_key=" + encode(continuation);
        return parsePage(request("GET", path, null));
    }
    static Page parsePage(JsonNode page) {
        if (!page.path("transactions").isArray()) throw new BankingException(502, "Missing transaction list");
        List<Entry> entries = new ArrayList<>();
        int skipped = 0;
        for (JsonNode t : page.path("transactions")) {
            if (!"BOOK".equals(text(t, "status"))) continue;
            String ref = text(t, "entry_reference");
            String date = text(t, "booking_date");
            if (date == null) date = text(t, "value_date");
            if (date == null) date = text(t, "transaction_date");
            // transaction_id is explicitly NOT a stable identifier in Enable Banking.
            if (ref == null || date == null) { skipped++; continue; }
            String direction = required(t, "credit_debit_indicator");
            if (!Set.of("CRDT", "DBIT").contains(direction)) throw new BankingException(502, "Invalid transaction direction");
            Money amount = money(t.path("transaction_amount"));
            if (amount.amount().signum() == 0) { skipped++; continue; }
            if (amount.amount().signum() < 0) throw new BankingException(502, "Unexpected negative transaction amount");
            List<String> lines = new ArrayList<>();
            for (JsonNode line : t.path("remittance_information")) lines.add(line.asText());
            String description = String.join(" ", lines);
            if (description.isBlank()) description = Optional.ofNullable(text(t.path(direction.equals("CRDT") ? "debtor" : "creditor"), "name")).orElse("Bank transaction");
            entries.add(new Entry(ref, amount, direction.equals("CRDT"), LocalDate.parse(date), description.substring(0, Math.min(255, description.length()))));
        }
        return new Page(entries, text(page, "continuation_key"), skipped);
    }
    @Override public Money balance(String uid, String currency) {
        return parseBalance(request("GET", "/accounts/" + encode(uid) + "/balances", null), currency);
    }
    static Money parseBalance(JsonNode response, String currency) {
        JsonNode balances = response.path("balances");
        if (!balances.isArray()) throw new BankingException(502, "Enable Banking response missing balances");
        for (String type : List.of("ITBD", "CLBD")) {
            for (JsonNode b : balances) {
                if (type.equals(text(b, "balance_type")) && currency.equals(text(b.path("balance_amount"), "currency")))
                    return money(b.path("balance_amount"));
            }
        }
        return null;
    }
    @Override public void disconnect(String session) { request("DELETE", "/sessions/" + encode(session), null); }
    private static Money money(JsonNode n) { return Money.of(new BigDecimal(required(n, "amount")), required(n, "currency")); }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private static String text(JsonNode n, String field) {
        JsonNode v = n.path(field); return v.isMissingNode() || v.isNull() || v.asText().isBlank() ? null : v.asText();
    }
    private static String required(JsonNode n, String field) {
        String v = text(n, field);
        if (v == null) throw new BankingException(502, "Enable Banking response missing " + field);
        return v;
    }
}
