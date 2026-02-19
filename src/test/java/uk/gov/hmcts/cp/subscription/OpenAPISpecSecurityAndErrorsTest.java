package uk.gov.hmcts.cp.subscription;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAPISpecSecurityAndErrorsTest {

    private static final String SPEC_PATH = "/openapi/openapi-spec.yml";

    @Test
    void spec_should_have_bearer_auth_security_scheme() throws Exception {
        String spec = loadSpec();
        assertThat(spec).contains("bearerAuth");
        assertThat(spec).contains("bearer");
        assertThat(spec).contains("JWT");
        assertThat(spec).contains("Authorization");
        assertThat(spec).contains("CLIENT_ID");
        assertThat(spec).contains("azp");
    }

    @Test
    void spec_should_have_401_and_500_error_responses() throws Exception {
        String spec = loadSpec();
        assertThat(spec).contains("Missing token");
        assertThat(spec).contains("Valid token but missing azp claim");
        assertThat(spec).contains("InternalServerError");
        assertThat(spec).contains("unauthorized");
        assertThat(spec).contains("internal_error");
    }

    @Test
    void spec_should_have_security_required_on_subscription_paths() throws Exception {
        String spec = loadSpec();
        assertThat(spec).contains("security:");
        assertThat(spec).containsPattern("bearerAuth\\s*:\\s*\\[\\s*\\]");
    }

    private String loadSpec() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SPEC_PATH)) {
            assertThat(in).as("OpenAPI spec at %s", SPEC_PATH).isNotNull();
            try (Scanner s = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
        }
    }
}
