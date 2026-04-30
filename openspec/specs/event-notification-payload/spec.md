### Requirement: EventNotificationPayload includes eventType
The webhook callback payload (`EventNotificationPayload`) SHALL include an `eventType` field identifying which event triggered the notification. The field MUST be required (never null or absent), and its value MUST conform to the `EventType` schema defined in the OpenAPI spec.

#### Scenario: Webhook callback body contains eventType
- **WHEN** a subscriber webhook is called following a document event
- **THEN** the request body MUST contain a non-null `eventType` field whose value is a valid `EventType` string

#### Scenario: eventType matches the triggering event
- **WHEN** a `PRISON_COURT_REGISTER_GENERATED` event triggers a notification
- **THEN** the `eventType` field in the callback payload MUST equal `PRISON_COURT_REGISTER_GENERATED`

#### Scenario: Generated model field is present
- **WHEN** the OpenAPI spec is used to generate the `EventNotificationPayload` Java model
- **THEN** the generated class SHALL have a field named `eventType` of the correct type

#### Scenario: Spec example includes eventType
- **WHEN** the OpenAPI spec example for `EventNotificationPayload` is evaluated
- **THEN** it SHALL include a valid `eventType` value representative of a real event type