# Changelog

## [Unreleased]

### New Features

- **`POST /client-subscriptions/{clientSubscriptionId}/secret/rotate`**: New endpoint to rotate the HMAC signing secret for a subscription. Request body: `RotateSecretRequest` (containing `keyId`). Response: `HmacCredentials` with the same `keyId` and a newly generated `secret` (returned once, cannot be retrieved again). Returns `404` if the `keyId` is not found for the subscription, `403` if the subscription does not belong to the calling client.

### Breaking Changes

- **`EventNotificationPayload`**: Added required `eventType` field (type: `EventType` string). Webhook callback payloads now always include the event type that triggered the notification. Consumers must update their deserialization models to accept this field.