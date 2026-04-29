## Why

Subscribers currently have no way to rotate their HMAC signing secret. A dedicated rotation endpoint lets clients cycle credentials on a schedule or in response to a potential compromise.

## What Changes

- Add `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate` endpoint (tagged "Internal" consistent with notification endpoint)
- Request body: `RotateSecretRequest` containing `keyId` (the current key identifier to rotate)
- Response body: `HmacCredentials` with the same `keyId` (unchanged) and a newly generated `secret`
- Response `200`: rotation successful, new secret returned in body
- Response `404 Not Found`: `keyId` does not match any known key for that subscription
- Response `403 Forbidden`: the subscription does not belong to the calling client

**NOTE**: Like the original secret at subscription creation, the new secret is returned **once** and cannot be retrieved again.

## Capabilities

### New Capabilities

- `secret-rotation`: Defines the shape, security constraints, and error conditions for the HMAC secret rotation endpoint — including request contract (`keyId`), response contract (reusing `HmacCredentials`), and the 404/403 error cases.

### Modified Capabilities

## Impact

- `openapi-spec.yml`: new path `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate`, new `RotateSecretRequest` schema, reuses existing `HmacCredentials` schema for response
- Generated model: `RotateSecretRequest` added; `HmacCredentials` already exists and is unchanged
- `OpenAPISpecTest`: assertions for new endpoint method signature and `RotateSecretRequest` model fields
- Downstream consumers (e.g. Remand and Sentence Service) gain the ability to rotate HMAC secrets without recreating subscriptions