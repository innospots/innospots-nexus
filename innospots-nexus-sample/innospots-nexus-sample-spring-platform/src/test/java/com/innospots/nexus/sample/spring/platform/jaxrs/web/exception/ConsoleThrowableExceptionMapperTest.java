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

class ConsoleThrowableExceptionMapperTest extends ConsoleJaxRsWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TokenIssuer tokenIssuer;

    @Test
    void mapsUncheckedExceptionToSystemErrorLegacyR() throws Exception {
        String token = ConsoleJaxRsWebTestTokens.platformAccessToken(tokenIssuer);
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/jaxrs-web-test/runtime-error",
                Map.of("X-Request-Id", "mapper-throwable-001"),
                token);
        assertThat(response.statusCode()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.headers().firstValue("X-Request-Id")).contains("mapper-throwable-001");
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isFalse();
        assertThat(root.get("code").asText()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode());
    }
}
