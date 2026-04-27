## Why

`EventNotificationPayload` — the body POSTed to subscriber webhook callbacks — currently omits the `eventType` field, so consumers cannot determine which event triggered the notification without out-of-band knowledge. Adding `eventType` lets subscribers route, filter, or process notifications based on document type in a self-contained way.

## What Changes

- Add a required `eventType` field (referencing the existing `EventType` schema) to `EventNotificationPayload` in `openapi-spec.yml`
- Update the `EventNotificationPayload` example in the spec to include a representative `eventType` value
- Update `OpenAPISpecTest` to assert the new field is present on the generated model

**BREAKING**: Existing consumers of the webhook callback will receive a new required field in the payload. Consumers must update their deserialization/handling to accept `eventType`.

## Capabilities

### New Capabilities

- `event-notification-payload`: Defines the shape and required fields of the webhook callback payload delivered to subscribers, including `eventType` to identify the triggering event.

### Modified Capabilities

## Impact

- `openapi-spec.yml`: `EventNotificationPayload` schema gains a required `eventType` field
- Generated model `EventNotificationPayload.java` will include the new field (via OpenAPI Generator)
- `OpenAPISpecTest`: assertions must cover the new field
- Downstream consumers (e.g. Remand and Sentence Service) receiving webhook callbacks will see the new field in the request body
