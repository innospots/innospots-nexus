package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestTokens;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleAuthenticationFilterTest extends ConsoleJaxRsWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TokenIssuer tokenIssuer;

    @Test
    void permitAllOpenApiPathDoesNotRequireToken() throws Exception {
        HttpResponse<String> response = http.get("/openapi/jaxrs-web-test/ping");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("ping");
    }

    @Test
    void securedPathWithoutTokenReturns401LegacyBody() throws Exception {
        HttpResponse<String> response = http.get("/console/jaxrs-web-test/secured-echo");
        assertThat(response.statusCode()).isEqualTo(401);
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isFalse();
        assertThat(root.get("code").asText()).contains("0007");
    }

    @Test
    void securedPathWithValidBearerTokenSucceeds() throws Exception {
        String token = ConsoleJaxRsWebTestTokens.platformAccessToken(tokenIssuer);
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/jaxrs-web-test/secured-echo",
                null,
                token);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("secured");
    }

    @Test
    void securedPathWithMalformedAuthorizationReturns401() throws Exception {
        HttpResponse<String> response = http.get(
                "/console/jaxrs-web-test/secured-echo",
                java.util.Map.of("Authorization", "Token not-bearer"));
        assertThat(response.statusCode()).isEqualTo(401);
    }
}
