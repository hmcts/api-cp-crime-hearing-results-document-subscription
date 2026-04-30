## Context

The subscription creation endpoint (`POST /client-subscriptions`) returns `HmacCredentials` (a `keyId` and `secret`) once. Currently there is no way to rotate the secret. This change adds a dedicated rotation endpoint that issues a new secret for an existing subscription.

The `HmacCredentials` schema already exists. The only new schema needed is `RotateSecretRequest` for the request body.

## Goals

**Goals:**
- Allow a client to rotate the HMAC secret for a subscription without disrupting the subscription
- Return the new secret in the same `HmacCredentials` shape used at creation
- Enforce ownership: only the client that owns the subscription may rotate its secret
- Validate the `keyId` in the request matches a known key for that subscription

## Decisions

### Path: `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate`
Follows the existing resource hierarchy (`/client-subscriptions/{id}/...`). The verb `rotate` as a sub-resource action is consistent with REST conventions for non-CRUD operations.

### Request body: `RotateSecretRequest` with `keyId`
Requiring `keyId` in the request acts as confirmation that the caller knows the current key identifier, preventing accidental rotation. A keyless `POST` was considered but rejected — it provides no guard against unintentional calls.

### Response: reuse `HmacCredentials`, status 200
`HmacCredentials` already carries both `keyId` and `secret` fields. Reusing it avoids a new response model. Status 200 (not 201) is correct — this is an update operation, not resource creation.

### 404 vs 403 error distinction
- `404 Not Found`: the `keyId` in the request body is not recognised for the given `clientSubscriptionId` — the resource cannot be found
- `403 Forbidden`: the `clientSubscriptionId` exists but does not belong to the calling client (authenticated but unauthorised)

This distinction follows the principle of leaking minimal information: a valid client sees 403 for subscriptions that aren't theirs; an invalid `keyId` within their own subscription returns 404.

## Risks / Trade-offs

- **Secret returned once** → Caller must persist immediately; no recovery path. Mitigation: document clearly in spec and response description, consistent with creation behaviour.
- **No overlap window** → Callbacks signed with the old key will fail immediately after rotation. Mitigation: out of scope for this change.
- **`keyId` unchanged after rotation** → The key identifier stays the same; only the secret value changes. This simplifies consumer-side key lookup but means `keyId` alone cannot distinguish old from new secret.