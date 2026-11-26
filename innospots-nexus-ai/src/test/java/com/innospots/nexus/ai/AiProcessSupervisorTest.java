package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.ai.config.AiRuntimeConfig;
import com.innospots.nexus.ai.runtime.AiProcessSupervisor;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;

class AiProcessSupervisorTest {

    @Test
    void startsAndStopsNodeRuntime() throws Exception {
        Path dir = Files.createTempDirectory("nexus-ai-supervisor");
        Path runtime = dir.resolve("runtime");
        Files.createDirectories(runtime.resolve("bin"));
        Files.createDirectories(runtime.resolve("dist"));
        Files.writeString(runtime.resolve("dist/server.mjs"), "// placeholder");
        Files.writeString(runtime.resolve("package.json"),
                "{\"name\":\"@innospots/nexus-ai-runtime\",\"version\":\"0.0.0-test\"}");
        Files.writeString(runtime.resolve("bin/nexus-ai-runtime.mjs"), String.join("\n",
                "import http from 'node:http';",
                "import { writeFileSync } from 'node:fs';",
                "const args = process.argv.slice(2);",
                "const value = (name, fallback) => { const i = args.indexOf(name); return i >= 0 ? args[i + 1] : fallback; };",
                "const ready = value('--ready-file');",
                "const server = http.createServer((req, res) => {",
                "  if (req.url === '/v1/health') { res.writeHead(200, {'content-type':'application/json'}); res.end('{\"status\":\"UP\"}'); }",
                "  else { res.writeHead(404); res.end(); }",
                "});",
                "server.listen(Number(value('--port', '0')), value('--host', '127.0.0.1'), () => {",
                "  const port = server.address().port;",
                "  writeFileSync(ready, JSON.stringify({event:'listening',host:'127.0.0.1',port,version:'0.0.0-test',configVersion:'sha256:test'}));",
                "});",
                "process.on('SIGTERM', () => server.close(() => process.exit(0)));",
                ""));

        System.setProperty("nexus.ai.runtime.version", "0.0.0-test");
        try {
            AiRuntimeConfig config = new AiRuntimeConfig(
                    dir.resolve("data"), dir.resolve("config"), runtime, "node", "127.0.0.1", 0,
                    Duration.ofSeconds(10), Duration.ofSeconds(5), 1,
                    AiRuntimeConfig.StartupPolicy.EAGER, Duration.ofMillis(500), Duration.ofSeconds(1),
                    Duration.ofMillis(100), 3, false, null, Map.of());
            AiProcessSupervisor supervisor = new AiProcessSupervisor(config);
            try {
                supervisor.start();
                assertThat(supervisor.status()).isEqualTo(AiRuntimeStatus.RUNNING);
                assertThat(supervisor.health().ready()).isTrue();
                assertThat(supervisor.endpoint()).isPresent();
            } finally {
                supervisor.stop();
            }
            assertThat(supervisor.status()).isEqualTo(AiRuntimeStatus.STOPPED);
        } finally {
            System.clearProperty("nexus.ai.runtime.version");
        }
    }
}
