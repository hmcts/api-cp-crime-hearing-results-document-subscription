## 1. OpenAPI Spec — New Schema

- [x] 1.1 Add `RotateSecretRequest` schema to `components/schemas` in `openapi-spec.yml` with a required `keyId` string field
- [x] 1.2 Add a `rotateSecretRequest` example to `components/examples` with a representative `keyId` value

## 2. OpenAPI Spec — New Endpoint

- [x] 2.1 Add `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate` path to `openapi-spec.yml` with `operationId: rotateClientSubscriptionSecret`, tagged `Subscription`
- [x] 2.2 Add `clientSubscriptionId` path parameter (reuse `$ref: '#/components/parameters/ClientSubscriptionId'` if it exists, otherwise inline)
- [x] 2.3 Add `requestBody` referencing `RotateSecretRequest` schema and the `rotateSecretRequest` example
- [x] 2.4 Add `200` response referencing `HmacCredentials` schema with description noting the secret is returned once
- [x] 2.5 Add `400` response referencing `ErrorResponse` for missing/invalid `keyId` in request body
- [x] 2.6 Add `401`, `403`, and `404` responses consistent with other endpoints in the spec
- [x] 2.7 Add a `rotateSecretResponse` example to `components/examples` showing `HmacCredentials` with the same `keyId` and a new `secret`

## 3. Test Updates

- [x] 3.1 Update `OpenAPISpecTest` to assert that the generated `RotateSecretRequest` model has a field named `keyId` of type `String`
- [x] 3.2 Update `OpenAPISpecTest` to assert that `SubscriptionApi` (or equivalent generated interface) has a method for `rotateClientSubscriptionSecret`
- [x] 3.3 Run `./gradlew test` to confirm all tests pass

## 4. Build and Lint Verification

- [x] 4.1 Run `./gradlew build -x test -DAPI_SPEC_VERSION=local` to confirm the spec generates cleanly and the new model compiles
- [x] 4.2 Run `spectral lint "src/main/resources/openapi/*.{yml,yaml}"` to confirm the spec passes linting

## 5. Changelog

- [x] 5.1 Add an entry to `CHANGELOG.md` documenting the new `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate` endpoint