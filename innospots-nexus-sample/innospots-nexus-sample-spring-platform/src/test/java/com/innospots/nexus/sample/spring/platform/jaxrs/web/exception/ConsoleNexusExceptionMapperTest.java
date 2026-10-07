package com.innospots.nexus.sample.spring.platform.jaxrs.web.exception;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestTokens;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleNexusExceptionMapperTest extends ConsoleJaxRsWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TokenIssuer tokenIssuer;

    @Test
    void mapsNexusExceptionToLegacyRWithHttpStatus() throws Exception {
        String token = ConsoleJaxRsWebTestTokens.platformAccessToken(tokenIssuer);
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/jaxrs-web-test/nexus-error",
                Map.of("X-Request-Id", "mapper-nexus-001"),
                token);
        assertThat(response.statusCode()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
        assertThat(response.headers().firstValue("X-Request-Id")).contains("mapper-nexus-001");
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isFalse();
        assertThat(root.get("code").asText()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
        assertThat(root.get("message").asText()).contains("bad parameter");
    }
}
