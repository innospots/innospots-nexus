package com.innospots.nexus.sample.spring.platform.openapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.sample.spring.platform.openapi.support.OpenApiJerseyTestApplication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 嵌入式 Web 环境下验证文档地址、Scalar 脚本与 OpenAPI 目录 HTTP 接口。
 */
@SpringBootTest(
        classes = OpenApiJerseyTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "scalar.path=/openapi/ui",
        "scalar.enabled=true"
})
class SamplePlatformOpenApiHttpTest {

    private static final String PLATFORM_SPEC_ID = "innospots-nexus-platform";

    private static final String CONSOLE_SPEC_ID = "innospots-nexus-console";

    private static final String DOCS_PATH = "/openapi/ui";

    private static final String SCRIPT_PATH = "/openapi/ui/scalar.js";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void documentationPageIsAvailableAtConfiguredPath() throws Exception {
        HttpResponse<String> response = get(DOCS_PATH);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("text/html");
        assertThat(response.body()).containsIgnoringCase("scalar");
        assertThat(response.body()).contains(PLATFORM_SPEC_ID);
    }

    @Test
    void scalarScriptIsServedFromJar() throws Exception {
        HttpResponse<String> response = get(SCRIPT_PATH);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("javascript");
        String body = response.body();
        assertThat(body.length()).isGreaterThan(1000);
        assertThat(body).doesNotContain("<!DOCTYPE html>");
    }

    @Test
    void openApiSpecListEndpointReturnsBundledModules() throws Exception {
        HttpResponse<String> response = get("/openapi/specs");
        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isTrue();

        Set<String> specIds = new HashSet<>();
        for (JsonNode item : root.get("data")) {
            specIds.add(item.get("specId").asText());
        }
        assertThat(specIds).contains(PLATFORM_SPEC_ID, CONSOLE_SPEC_ID);
    }

    @Test
    void openApiSpecDetailEndpointReturnsPlatformYaml() throws Exception {
        HttpResponse<String> response = get("/openapi/specs/" + PLATFORM_SPEC_ID);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type").orElse(""))
                .contains("yaml");

        String yaml = response.body();
        assertThat(yaml).contains("openapi:");
        assertThat(yaml).contains("paths:");
        assertThat(yaml).contains("/platform/tenants");
    }

    @Test
    void openApiSpecDetailEndpointReturnsConsoleYaml() throws Exception {
        HttpResponse<String> response = get("/openapi/specs/" + CONSOLE_SPEC_ID);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("/openapi/specs");
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
