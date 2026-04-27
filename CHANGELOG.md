# Changelog

## [Unreleased]

### Breaking Changes

- **`EventNotificationPayload`**: Added required `eventType` field (type: `EventType` string). Webhook callback payloads now always include the event type that triggered the notification. Consumers must update their deserialization models to accept this field.