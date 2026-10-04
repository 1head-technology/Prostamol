# Enable Banking account sync

Prostamol now exposes authenticated endpoints for bank selection, consent, account import,
transaction sync, connection status, and disconnect. This repository contains the backend;
the browser application must implement the bank picker and callback route described below.

## Enable Banking setup

1. In the [Enable Banking control panel](https://enablebanking.com/cp/applications), register
   an API application. A website login alone does not provide API credentials.
2. Start with Sandbox. Set the application's redirect URL to the exact frontend callback,
   for example `http://localhost:3000/banking/callback`.
3. Save the downloaded RSA private key outside the repository. Configure its application
   ID and absolute file path on the backend. The key must be PKCS#8 PEM (`BEGIN PRIVATE KEY`).
4. Set these backend environment values and restart:

```properties
ENABLE_BANKING_ENABLED=true
ENABLE_BANKING_APPLICATION_ID=<application UUID>
ENABLE_BANKING_PRIVATE_KEY_PATH=/absolute/server/path/application.pem
ENABLE_BANKING_REDIRECT_URL=http://localhost:3000/banking/callback
ENABLE_BANKING_HISTORY_DAYS=90
ENABLE_BANKING_SYNC_ENABLED=true
ENABLE_BANKING_SYNC_DELAY_MS=21600000
```

For Docker, mount the key read-only and set `ENABLE_BANKING_PRIVATE_KEY_PATH` to the
container path. For example, add a Compose override with a `volumes` entry under
`services.prostamol`: `/absolute/host/path/application.pem:/run/secrets/enable-banking.pem:ro`.
The existing production Compose file does not automatically mount this file. PEM files
are ignored by Git and excluded from Docker build contexts.

The server signs RS256 application JWTs. Its private key and the provider session IDs
never belong in browser configuration. HTTPS is required for redirect URLs except localhost.
Sandbox and production use the same API origin; the registered application determines
the environment. Real accounts require production access and bank consent.
See the official [quick start](https://enablebanking.com/docs/api/quick-start/) and
[API reference](https://enablebanking.com/docs/api/reference/) for registration and bank capabilities.

## Frontend contract

Every endpoint requires the existing Prostamol bearer JWT. Ownership is taken from that
JWT, never from a user ID supplied in the request.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/banking/banks?country=IT` | Available banks and maximum consent validity |
| POST | `/api/v1/banking/connections` | Start bank authorization |
| POST | `/api/v1/banking/connections/complete` | Exchange callback code and import accounts and history |
| GET | `/api/v1/banking/connections` | Status, expiry, sync timestamp, errors, skipped count, linked account IDs and balances |
| POST | `/api/v1/banking/connections/{id}/sync` | Retry or manually refresh |
| DELETE | `/api/v1/banking/connections/{id}` | Revoke provider session and stop syncing; retain imported data |

Start authorization:

```json
{"bankName":"<name from banks endpoint>","country":"IT","psuType":"personal"}
```

`psuType` is required: `personal` or `business`. Navigate the browser to the returned `url`.
The backend stores a hash of a random state bound to the initiating user with a 30-minute
expiry. The callback is a frontend route, not an unauthenticated API endpoint.

On return, read `state` and `code` from the query string and POST them to `connections/complete`
with the same user's Prostamol JWT:

```json
{"state":"<returned state>","code":"<returned code>"}
```

On cancellation, send `{"state":"<returned state>","error":"access_denied"}` instead.
Send either code or error. Remove callback parameters from the browser URL after processing.
If the app login expired, sign in as the same user before completing the callback.
State is consumed once. An exchange failure requires a new connection attempt. If the initial
sync fails after session creation, list connections and use the sync endpoint; the session
and accounts remain available for retry.

After completion, refresh the existing `/api/v1/accounts` and `/api/v1/transactions` views.
Connection responses include local `accountId` values to associate accounts with their bank.
Display `lastSyncedAt`, `lastError`, `validUntil`, and `skippedTransactions`. Prompt the user
to reconnect after expiry or revoked consent. Starting a new authorization reuses local
accounts when the bank returns the same primary identification hash and currency.

## Import behavior and limitations

- Only booked transactions are imported. Credits become income and debits become expenses.
  Transactions are uncategorized; transfers are not automatically paired across accounts.
- All transaction pages are fetched from the connection's fixed history start through today
  (UTC). Re-fetching this window catches late bookings. History availability depends on the bank.
- Stable account hashes and per-account entry references prevent duplicate imports, including
  concurrent syncs. Provider `transaction_id` is not suitable for deduplication. Booked entries
  missing a stable reference or date, and zero-value entries, are skipped and counted.
- Existing imported transactions are preserved, including user edits. Provider corrections to
  already imported references are not applied. Import receipts keep deleted transactions deleted.
  Deleting a linked local account suppresses future imports for that identity.
- A sync commits all its transaction imports, receipts, and balance snapshots together. A failed
  page rolls back that sync and records an error. Other connections can still sync.
- The default background interval is six hours, starting six hours after startup. Disabling the
  scheduler retains manual sync. Expired/disconnected connections are not fetched. Provider-reported expired sessions are marked `EXPIRED`; closed, revoked, or missing
  sessions are marked `FAILED`. These connections stop background retries and require new authorization.
  Other provider failures retain the current status and can be retried.
- Bank account balances use the latest bank snapshot (`ITBD`, falling back to `CLBD`), including
  negative balances. They are not computed from incomplete imported history or manual entries.
  If the bank supplies no supported booked balance, the existing balance endpoint returns 409;
  the connection's balance fields are null. After disconnect, the last snapshot remains and can
  be stale; display its `balanceUpdatedAt`. Manual accounts keep their existing balance calculation.
- Session IDs are stored in the database, so database backups need the same protection as other
  banking data. Neither provider response bodies nor authorization codes are logged by this module.
- Sync currently performs bounded provider calls while holding database locks. It suits a small
  personal installation; large installations should move to queued, leased per-account jobs.

Hibernate creates the three additional tables using the project's existing `ddl-auto=update`
configuration. No live bank was connected during implementation. Automated tests use a mocked
provider and an isolated H2 database; sandbox validation still requires your application setup
and the frontend callback.

## Troubleshooting sandbox sync

A successful authorization does not guarantee subsequent account requests will succeed.
Some bank sandboxes expire sessions early or invalidate an older session when another is
created. See the [provider FAQ](https://enablebanking.com/docs/faq/).

Sync failures include recognized Enable Banking error codes (for example `EXPIRED_SESSION`)
in the API error and connection `lastError`. HTTP 401 alone does not prove consent expired.
For other authorization errors, inspect the failed request in the Enable Banking control
panel under Applications → Requests. Raw provider messages and details are not exposed.

A balance HTTP 409 means there is no saved supported booked balance for that account.
Check the connection error and last successful sync first: a failed sync rolls back all
new balances and imports, preserving any older snapshots. After a successful sync, an
account can still lack an `ITBD` or `CLBD` balance in its own currency. Other accounts can
have balances normally. Available balances are not substituted for booked balances, and
missing balances are not treated as zero.

Disconnect also completes locally when Enable Banking reports `EXPIRED_SESSION`,
`CLOSED_SESSION`, `REVOKED_SESSION`, or `SESSION_DOES_NOT_EXIST`: access has already ended.
Imported accounts, transactions, and the last balance snapshots remain available. Other
provider failures still leave the session stored so revocation can be retried.
