# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Repository Is

This is a **specification-first API** project for the Crime Hearing Results Document Subscription service (HMCTS). The OpenAPI spec (`src/main/resources/openapi/openapi-spec.yml`) is the source of truth. There is no main application code — only the spec, generated interfaces/models, and tests. Consumers import the published JAR to get auto-generated Spring API interfaces.

## Commands

### Build
```bash
./gradlew build -DAPI_SPEC_VERSION=<version>   # full build with tests
./gradlew build -x test                         # skip tests
```

`API_SPEC_VERSION` is required for full builds. In CI it is generated from git history via `hmcts/artefact-version-action`. Locally any string works (e.g. `-DAPI_SPEC_VERSION=local`).

### Test
```bash
./gradlew test                                                        # all tests
./gradlew test --tests uk.gov.hmcts.cp.subscription.OpenAPISpecTest   # single class
./gradlew test --tests "uk.gov.hmcts.cp.subscription.OpenAPISpecTest.notification_endpoint_should_have_expected_fields"  # single method
./gradlew check                                                       # tests + jacoco coverage (coverage report required — cannot be skipped)
```

### Code Quality
```bash
./gradlew pmdMain                                    # PMD static analysis
spectral lint "src/main/resources/openapi/*.{yml,yaml}"  # OpenAPI spec linting
```

## Architecture

### Notification Flow
1. Progression Service generates a Prison Court Register (PCR) document
2. PCR event triggers this service via `POST /notifications`
3. Service fetches PCR document from Material Service via time-limited SAS URL
4. Service resolves all registered subscribers
5. Service fans out delivery via Artemis Message Broker
6. Webhooks delivered through API Management with HMAC-SHA256 signed requests
7. Failed webhooks retry with exponential backoff; exhausted retries go to Dead Letter Queue

### Key Endpoints (defined in openapi-spec.yml)
- `GET /event-types` — list valid event types
- `POST /client-subscriptions` — register a webhook subscription (returns 201)
- `GET/PUT/DELETE /client-subscriptions/{clientSubscriptionId}` — manage subscriptions; `PUT` is a strict **full** update (no PATCH)
- `POST /notifications` — internal trigger for notification fanout (tagged "Internal")
- `GET /client-subscriptions/{clientSubscriptionId}/documents/{documentId}` — retrieve PDF documents

### Security
All endpoints require Bearer JWT + `Ocp-Apim-Subscription-Key` header. Callbacks include `X-Key-Id` and `X-Signature` (HMAC-SHA256) headers. The HMAC secret is returned **once** at subscription creation inside `HmacCredentials` and cannot be retrieved again.

### Code Generation
OpenAPI Generator (v7.21.0) reads `openapi-spec.yml` and generates:
- `uk.gov.hmcts.cp.openapi.api` — Spring interfaces (`SubscriptionApi`, `NotificationApi`)
- `uk.gov.hmcts.cp.openapi.model` — model classes (`ClientSubscription`, `EventPayload`, `HmacCredentials`, etc.)

Generated code lives in `build/generated/` and is not committed. Key generation details:
- All `OffsetDateTime` types are mapped to `java.time.Instant`
- Lombok `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor` are injected into every model class

### Dependencies
Only `io.swagger.core.v3:swagger-annotations` is declared as `implementation`. All other dependencies are `compileOnly` to prevent transitive conflicts in consuming services. See `docs/DEPENDENCIES.md` for how to add the JAR as a dependency.

### Test Structure
Three test classes in `src/test/java/uk/gov/hmcts/cp/subscription/`:
- **`OpenAPISpecTest`** — uses reflection to verify generated model fields and API interface method signatures match the spec (field types, names, method signatures)
- **`ValidateClientSubscriptionRequestTest`** — validates Jakarta validation annotations (HTTPS URL regex, event type list size = 1, null checks)
- **`SubscriptionKeySecurityTest`** — parses `openapi-spec.yml` at runtime with SnakeYAML; validates security schemes and that all subscription endpoints return 401/403

Test method names use underscores (e.g. `notification_endpoint_should_have_expected_fields`); this is intentional and permitted by the PMD ruleset.

### Gradle Configuration Modules
- `gradle/openapi.gradle` — OpenAPI code generation settings (packages, type mappings, lombok injection)
- `gradle/java.gradle` — Java 25 (temurin), `-Xlint:unchecked -Werror` (warnings are compiler errors)
- `gradle/test.gradle` — JUnit Platform, Jacoco, fail-fast enabled
- `gradle/pmd.gradle` — PMD rules (see `.github/pmd-ruleset.xml`); generated code is excluded
- `gradle/jar.gradle` — JAR packaging; includes `CHANGELOG.md` in `META-INF` and CycloneDX SBOM (`bom.json`)
- `gradle/repositories.gradle` — GitHub Packages + Azure Artifacts; publishes to both

### CI/CD
- **`ci-draft.yml`** — runs on PRs/main; publishes draft spec to SwaggerHub and draft artifact
- **`ci-released.yml`** — runs on GitHub release; publishes release spec and artifact (`-x test` is passed to gradle on release)
- **`lint-openapi.yml`** — validates spec (spectral), JSON schema examples (ajv), JSON linting; rejects internal HMCTS URLs (`cjscp.org.uk`, `hmcts.net`, `justice.gov.uk`, etc.) in the spec

CI injects the generated artifact version into `openapi-spec.yml` (via `hmcts/update-openapi-version`) before the build/publish steps run.

Artifact publishing requires `GITHUB_TOKEN`, `AZURE_DEVOPS_ARTIFACT_USERNAME`, and `AZURE_DEVOPS_ARTIFACT_TOKEN` environment variables.

## Key Docs
- `docs/NOTIFICATIONS.md` — detailed notification flow with sequence diagram
- `docs/DEPENDENCIES.md` — how to consume the published JAR as a dependency
- `docs/NOTIFICATION_REQUIREMENTS.md` — functional requirements (consumer: Remand and Sentence Service)