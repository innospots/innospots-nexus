package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "nexus.console.web.cors.enabled=true")
class ConsoleCorsFilterTest extends ConsoleJaxRsWebIntegrationTest {

    @Test
    void optionsPreflightReturnsCorsHeaders() throws Exception {
        HttpResponse<String> response = http.options(
                "/openapi/jaxrs-web-test/ping",
                Map.of("Origin", "http://localhost:3000"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains("*");
        assertThat(response.headers().firstValue("Access-Control-Allow-Methods")).isPresent();
    }

    @Test
    void getWithOriginAddsCorsResponseHeaders() throws Exception {
        HttpResponse<String> response = http.get(
                "/openapi/jaxrs-web-test/ping",
                Map.of("Origin", "http://localhost:3000"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isPresent();
    }
}
