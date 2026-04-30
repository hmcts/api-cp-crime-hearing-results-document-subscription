## 1. OpenAPI Spec Update

- [x] 1.1 Add `eventType` to `EventNotificationPayload.required` list in `openapi-spec.yml`
- [x] 1.2 Add `eventType` property to `EventNotificationPayload.properties` referencing `$ref: '#/components/schemas/EventType'`
- [x] 1.3 Add or update the `EventNotificationPayload` spec example to include a representative `eventType` value (e.g. `PRISON_COURT_REGISTER_GENERATED`)

## 2. Test Updates

- [x] 2.1 Update `OpenAPISpecTest` to assert that the generated `EventNotificationPayload` model has a field named `eventType`
- [x] 2.2 Run `./gradlew test` to confirm all tests pass with the new field

## 3. Build Verification

- [x] 3.1 Run `./gradlew build -x test` to confirm the spec generates cleanly and the model compiles
- [x] 3.2 Run `spectral lint "src/main/resources/openapi/*.{yml,yaml}"` to confirm the spec passes linting

## 4. Changelog

- [x] 4.1 Add a breaking change entry to `CHANGELOG.md` documenting the addition of the required `eventType` field to `EventNotificationPayload`