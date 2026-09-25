package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleRequestContextFilterTest extends ConsoleJaxRsWebIntegrationTest {

    @Test
    void responseIncludesGeneratedRequestIdWhenHeaderMissing() throws Exception {
        HttpResponse<String> response = http.get("/openapi/jaxrs-web-test/ping");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Request-Id")).isPresent();
        assertThat(response.headers().firstValue("X-Request-Id").orElse("")).isNotBlank();
    }

    @Test
    void responseEchoesClientRequestId() throws Exception {
        String requestId = "test-request-id-001";
        HttpResponse<String> response = http.get(
                "/openapi/jaxrs-web-test/ping",
                Map.of("X-Request-Id", requestId));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Request-Id")).contains(requestId);
    }
}
