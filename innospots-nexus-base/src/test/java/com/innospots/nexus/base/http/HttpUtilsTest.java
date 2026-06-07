package com.innospots.nexus.base.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpUtilsTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", this::echo);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createsClientWithTimeoutsAndDefaultHeaders() throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .connectTimeout(Duration.ofSeconds(2))
                .responseTimeout(Duration.ofSeconds(3))
                .userAgent("nexus-test")
                .defaultHeader("X-Nexus", "base")
                .build()) {
            HttpResult result = HttpUtils.get(client, baseUrl + "/echo", Map.of("X-Request", "get"));

            assertThat(result.statusCode()).isEqualTo(200);
            assertThat(result.body()).contains("method=GET");
            assertThat(result.body()).contains("nexus=base");
            assertThat(result.body()).contains("request=get");
            assertThat(result.isSuccessful()).isTrue();
        }
    }

    @Test
    void postsJsonBody() throws IOException {
        HttpResult result = HttpUtils.postJson(baseUrl + "/echo", "{\"name\":\"nexus\"}");

        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).contains("method=POST");
        assertThat(result.body()).contains("contentType=application/json");
        assertThat(result.body()).contains("body={\"name\":\"nexus\"}");
    }

    private void echo(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String response = "method=" + exchange.getRequestMethod()
                + ";contentType=" + exchange.getRequestHeaders().getFirst("Content-Type")
                + ";nexus=" + exchange.getRequestHeaders().getFirst("X-Nexus")
                + ";request=" + exchange.getRequestHeaders().getFirst("X-Request")
                + ";body=" + body;
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
