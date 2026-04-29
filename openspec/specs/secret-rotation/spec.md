### Requirement: Secret rotation endpoint exists
The API SHALL expose `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate` to allow clients to rotate the HMAC signing secret for a subscription.

#### Scenario: Endpoint is reachable
- **WHEN** a client sends `POST /client-subscriptions/{clientSubscriptionId}/secret/rotate` with a valid `keyId`
- **THEN** the API MUST process the request and return a response

### Requirement: RotateSecretRequest body contains keyId
The request body MUST be a `RotateSecretRequest` object containing a required `keyId` field identifying the current key to rotate.

#### Scenario: Request with keyId is accepted
- **WHEN** a valid `keyId` matching the subscription's current key is provided in the request body
- **THEN** the API MUST accept the request and proceed with rotation

#### Scenario: Request without keyId is rejected
- **WHEN** the request body omits the `keyId` field
- **THEN** the API MUST return `400 Bad Request`

### Requirement: Successful rotation returns new HmacCredentials with status 200
On successful rotation the API SHALL return `200 OK` with an `HmacCredentials` response body containing the unchanged `keyId` and a newly generated `secret`.

#### Scenario: Rotation returns 200 with new secret
- **WHEN** the rotation request is valid and the subscription is found
- **THEN** the response status MUST be `200`
- **AND** the response body MUST contain an `HmacCredentials` object with the same `keyId` and a new `secret` value

#### Scenario: Returned keyId is unchanged
- **WHEN** a rotation is performed
- **THEN** the `keyId` in the response MUST equal the `keyId` supplied in the request

#### Scenario: New secret differs from old secret
- **WHEN** a rotation is performed
- **THEN** the `secret` in the response MUST be a newly generated value distinct from the previous secret

### Requirement: Secret is returned once
The rotated secret SHALL be returned only in the rotation response and MUST NOT be retrievable by any subsequent GET operation, consistent with the original secret issued at subscription creation.

#### Scenario: Secret not exposed on GET after rotation
- **WHEN** `GET /client-subscriptions/{clientSubscriptionId}` is called after rotation
- **THEN** the response MUST NOT include the secret value

### Requirement: 404 returned for unknown keyId
The API SHALL return `404 Not Found` when the `keyId` in the request body does not match any known key for the specified subscription.

#### Scenario: Unknown keyId returns 404
- **WHEN** the `keyId` in the request does not correspond to a key registered for that `clientSubscriptionId`
- **THEN** the response status MUST be `404`

### Requirement: 403 returned when subscription does not belong to caller
The API SHALL return `403 Forbidden` when the `clientSubscriptionId` exists but the authenticated client is not the owner of that subscription.

#### Scenario: Subscription owned by different client returns 403
- **WHEN** an authenticated client requests rotation for a `clientSubscriptionId` that belongs to a different client
- **THEN** the response status MUST be `403`
