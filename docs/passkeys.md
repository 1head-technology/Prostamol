# Passkey sign-in

Existing users can enroll one or more passkeys after signing in with their password.
Passkey sign-in returns the same `{ token, user }` response as password sign-in.
Passwords remain available for recovery. Public user responses omit password hashes.
This repository contains the API; integrate the browser calls below into the frontend.

## Configuration

Set these variables before starting the API:

```dotenv
PASSKEY_RP_ID=app.example.com
PASSKEY_RP_NAME=Prostamol
PASSKEY_ORIGINS=https://app.example.com
```

The RP ID is the frontend hostname, without scheme, port, or path. Origins are exact
frontend origins including scheme and any nonstandard port; multiple origins can be
comma-separated. Only explicitly configured origins are accepted. Choose a stable RP
ID: changing it makes previously enrolled credentials unusable under the new ID.
Production frontend pages require HTTPS. Defaults support `http://localhost:3000`.

Hibernate creates `passkey_credentials` and `passkey_challenges` using the project's
existing `ddl-auto=update` policy. Credentials and pending ceremonies persist across
restarts and are shared by API instances using the same database and RP configuration.
Challenges expire after five minutes and are consumed atomically before verification;
failed verification also consumes the challenge. Expired rows are removed when the
next ceremony starts. Apply request rate limits at the deployment's gateway, especially
for the public options endpoint, which allocates a pending challenge.

## API

All paths below are relative to `/api/v1`. Protected calls require an
`Authorization: Bearer <token>` header. No cookies or server sessions are required.

| Method | Path | Authentication | Result |
| --- | --- | --- | --- |
| POST | `/passkeys/register/options` | JWT | `{ requestId, publicKey }` creation options |
| POST | `/passkeys/register/verify` | JWT | 201, passkey summary |
| GET | `/passkeys` | JWT | Current user's passkey summaries |
| DELETE | `/passkeys/{id}` | JWT | 204; deletes only a passkey owned by this user |
| POST | `/auth/passkeys/options` | Public | `{ requestId, publicKey }` request options |
| POST | `/auth/passkeys/verify` | Public | `{ token, user }` |

Registration verification body:

```json
{ "requestId": "UUID from options", "name": "My phone", "credential": { "...": "browser credential JSON" } }
```

Login verification body:

```json
{ "requestId": "UUID from options", "credential": { "...": "browser credential JSON" } }
```

A summary contains `id`, `name`, `createdAt`, and nullable `lastUsedAt`. Use its `id`
for deletion. Credential public keys and internal counters are never returned.
Deletion prevents future passkey sign-ins; it does not invalidate already issued JWTs
or remove the credential from the user's password manager. Invalid, expired, replayed,
or mismatched ceremonies return a generic 401; invalid request fields return 400.
Start a fresh ceremony after a failed verification attempt.

## Browser integration

These functions use the browser's native WebAuthn JSON conversion methods. Feature
detect them as shown and retain password sign-in on unsupported browsers. The browser
handles biometric/PIN prompts and passkey selection; no email is required for login.
Invoke these functions from the corresponding user action and display errors in the UI.

```js
const apiBase = 'http://localhost:8080/api/v1'; // Your API deployment URL

function supportsPasskeys() {
  const pk = globalThis.PublicKeyCredential;
  return globalThis.isSecureContext && !!pk?.parseCreationOptionsFromJSON &&
    !!pk?.parseRequestOptionsFromJSON && !!pk?.prototype.toJSON;
}

async function post(path, body, token) {
  const response = await fetch(`${apiBase}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body ?? {}),
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.message ?? 'Passkey request failed');
  return data;
}

async function enrollPasskey(token, name) {
  if (!supportsPasskeys()) throw new Error('Passkeys require a supported browser');
  const options = await post('/passkeys/register/options', {}, token);
  const credential = await navigator.credentials.create({
    publicKey: PublicKeyCredential.parseCreationOptionsFromJSON(options.publicKey),
  });
  if (!credential) throw new Error('Passkey creation was cancelled');
  // JSON.stringify invokes credential.toJSON(), encoding binary fields as base64url.
  return post('/passkeys/register/verify', {
    requestId: options.requestId, name, credential,
  }, token);
}

async function signInWithPasskey() {
  if (!supportsPasskeys()) throw new Error('Passkeys require a supported browser');
  const options = await post('/auth/passkeys/options');
  const credential = await navigator.credentials.get({
    publicKey: PublicKeyCredential.parseRequestOptionsFromJSON(options.publicKey),
  });
  if (!credential) throw new Error('Passkey sign-in was cancelled');
  return post('/auth/passkeys/verify', {
    requestId: options.requestId, credential,
  }); // Hand token and user to the frontend's existing sign-in handler.
}
```

Treat browser `NotAllowedError` as cancellation or timeout and let the user retry.
Enrollment requires discoverable credentials and user verification. Authentication
also requires user verification. Signature counters are verified and updated; zero
counters used by some synced passkeys are supported.

## Verification

`PasskeyIntegrationTests` uses an isolated H2 database, real generated P-256 keys,
CBOR registration responses, and signed assertions. It covers enrollment, JWT issuance,
invalid signatures/origins/challenges, missing user verification, wrong user handles,
replay/expiry, concurrent consumption, counters, duplicate enrollment, ownership, and
revocation. It does not automate a physical device's biometric prompt.

```sh
./mvnw -Dtest=PasskeyIntegrationTests test
```

Implementation references: [Yubico server guide](https://developers.yubico.com/java-webauthn-server/),
[WebAuthn JSON parsing](https://developer.mozilla.org/en-US/docs/Web/API/PublicKeyCredential/parseCreationOptionsFromJSON_static),
[credential JSON serialization](https://developer.mozilla.org/en-US/docs/Web/API/PublicKeyCredential/toJSON).
