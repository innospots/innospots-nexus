package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.ai.config.AiRuntimeConfig;
import com.innospots.nexus.ai.runtime.AiProcessSupervisor;

class AiNodeSmokeTest {

    @Test
    void javaSupervisorStartsRealNodeRuntimeAndCallsHealthAndAgents() throws Exception {
        Path runtime = Path.of("runtime").toAbsolutePath().normalize();
        assumeTrue(Files.isRegularFile(runtime.resolve("dist/server.mjs")), "node runtime dist not built");
        Path config = runtime.resolve("config");
        assumeTrue(Files.isDirectory(config), "node runtime config missing");

        Path data = Files.createTempDirectory("nexus-ai-smoke");
        System.setProperty("nexus.ai.runtime.version", "0.1.0-SNAPSHOT");
        try {
            AiRuntimeConfig runtimeConfig = new AiRuntimeConfig(
                    data, config, runtime, "node", "127.0.0.1", 0,
                    Duration.ofSeconds(20), Duration.ofSeconds(5), 1,
                    AiRuntimeConfig.StartupPolicy.EAGER, Duration.ofSeconds(2), Duration.ofSeconds(1),
                    Duration.ofMillis(200), 3, false, null, Map.of());
            AiProcessSupervisor supervisor = new AiProcessSupervisor(runtimeConfig);
            try {
                supervisor.start();
                URI base = supervisor.endpoint().orElseThrow().baseUri();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> health = client.send(
                        HttpRequest.newBuilder(base.resolve("/v1/health")).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                assertThat(health.statusCode()).isEqualTo(200);
                assertThat(health.body()).contains("\"status\":\"UP\"");
                HttpResponse<String> agents = client.send(
                        HttpRequest.newBuilder(base.resolve("/v1/agents")).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                assertThat(agents.statusCode()).isEqualTo(200);
                assertThat(agents.body()).contains("default");
            } finally {
                supervisor.stop();
            }
        } finally {
            System.clearProperty("nexus.ai.runtime.version");
        }
    }
}
