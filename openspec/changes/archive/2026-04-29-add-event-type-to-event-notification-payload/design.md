## Context

`EventNotificationPayload` is the request body POSTed to registered subscriber webhook URLs when a document event occurs. It currently carries case, defendant, document, and timestamp data but omits the event type. The `EventType` enum schema already exists in the spec (used by `EventPayload`, the internal notification trigger body). The addition is a single field referencing that existing schema.

## Goals / Non-Goals

**Goals:**
- Add `eventType` as a required field on `EventNotificationPayload`, referencing `#/components/schemas/EventType`
- Keep `EventType` as the single source of truth for valid event type values
- Update the spec example for `EventNotificationPayload` to include `eventType`
- Update `OpenAPISpecTest` to verify the new field exists on the generated model

**Non-Goals:**
- Changing the `EventType` enum values or adding new event types
- Modifying `EventPayload` (the internal trigger body — already has `eventType`)
- Adding filtering logic by event type (subscriber-side concern)

## Decisions

### Add `eventType` as required (not optional)

Every notification is triggered by a specific event. Emitting it optionally would force consumers to null-check a value that is always present, and would weaken the contract. Making it required is a breaking change but gives consumers a reliable field to act on.

**Alternative considered**: Make it optional (`nullable: true`) for backwards compatibility. Rejected because: (a) the field will always be populated by the server, so optionality is misleading; (b) optional fields invite consumers to ignore them, defeating the purpose.

### Reuse existing `EventType` schema via `$ref`

`EventType` is already defined and used by `EventPayload`. Reusing it avoids duplication and keeps the enum values in one place.

**Alternative considered**: Inline the type as `type: string` with no `$ref`. Rejected because it would diverge from `EventPayload` and require duplicate updates when new event types are added.

## Risks / Trade-offs

- **Breaking change for existing webhook consumers** → Mitigation: document the breaking change clearly in `CHANGELOG.md` and version the artifact accordingly. Consumers must update deserialization models before upgrading the JAR.
- **Generated model field ordering** → No risk: OpenAPI Generator adds fields in spec order; inserting `eventType` before or after existing fields has no semantic impact.

## Migration Plan

1. Update `openapi-spec.yml`: add `eventType` to `EventNotificationPayload.required` and `EventNotificationPayload.properties`
2. Update the spec example to include a representative `eventType` value
3. Update `OpenAPISpecTest` to assert the field is present
4. Bump `CHANGELOG.md` with a breaking change entry
5. Publish a new major or minor version (per team versioning policy) so downstream consumers can pin

Rollback: revert the spec change and republish the previous version.