package com.innospots.nexus.ai.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.innospots.nexus.ai.config.AiRuntimeConfig;

/**
 * Node 进程 supervisor。
 */
public final class AiProcessSupervisor implements AiRuntime {

    private static final Pattern PORT = Pattern.compile("\"port\"\\s*:\\s*(\\d+)");
    private static final Pattern VERSION = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CONFIG_VERSION = Pattern.compile("\"configVersion\"\\s*:\\s*\"([^\"]+)\"");

    private final AiRuntimeConfig config;
    private final RuntimeLocator locator = new RuntimeLocator();
    private final RuntimePackageValidator validator = new RuntimePackageValidator();
    private final NodeVersionChecker nodeChecker = new NodeVersionChecker();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final AtomicReference<AiRuntimeHealth> health = new AtomicReference<>(
            new AiRuntimeHealth(AiRuntimeStatus.STOPPED, Instant.now(), null, null, -1, null, -1, null, null));
    private ScheduledExecutorService monitor;

    private Process process;
    private AiEndpoint endpoint;
    private Path runtimeDir;
    private String nodeVersion;
    private String runtimeVersion;
    private String configVersion;
    private int failures;
    private int restarts;

    public AiProcessSupervisor(AiRuntimeConfig config) {
        this.config = config;
        config.validate();
    }

    @Override
    public synchronized void start() {
        if (health.get().state() == AiRuntimeStatus.RUNNING || health.get().state() == AiRuntimeStatus.STARTING) {
            return;
        }
        setState(AiRuntimeStatus.STARTING, null);
        try {
            Files.createDirectories(config.dataDir());
            runtimeDir = locator.locate(config.runtimeDir());
            runtimeVersion = validator.validate(runtimeDir);
            nodeVersion = nodeChecker.check(config.nodeExecutable());
            endpoint = launch();
            startMonitor();
            setState(AiRuntimeStatus.RUNNING, null);
        } catch (Exception e) {
            setState(AiRuntimeStatus.FAILED, e.getMessage());
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void stop() {
        if (health.get().state() == AiRuntimeStatus.STOPPED) {
            return;
        }
        setState(AiRuntimeStatus.STOPPING, null);
        if (monitor != null) {
            monitor.shutdownNow();
        }
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(config.shutdownTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }
        process = null;
        endpoint = null;
        try {
            Files.deleteIfExists(config.dataDir().resolve("runtime.json"));
        } catch (IOException ignored) {
            // best effort
        }
        setState(AiRuntimeStatus.STOPPED, null);
    }

    @Override
    public AiRuntimeStatus status() {
        return health.get().state();
    }

    @Override
    public AiRuntimeHealth health() {
        return health.get();
    }

    @Override
    public Optional<AiEndpoint> endpoint() {
        return Optional.ofNullable(endpoint);
    }

    private AiEndpoint launch() {
        Path readyFile = config.dataDir().resolve("runtime.json");
        try {
            Files.deleteIfExists(readyFile);
        } catch (IOException ignored) {
            // best effort
        }
        Path logDir = config.dataDir().resolve("logs");
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            throw new IllegalStateException("cannot create log directory", e);
        }
        List<String> command = new ArrayList<>();
        command.add(config.nodeExecutable());
        command.add(runtimeDir.resolve("bin").resolve("nexus-ai-runtime.mjs").toString());
        command.add("--host");
        command.add(config.bindHost());
        command.add("--port");
        command.add(String.valueOf(config.port()));
        command.add("--data-dir");
        command.add(config.dataDir().toString());
        command.add("--config-dir");
        command.add(config.configDir().toString());
        command.add("--ready-file");
        command.add(readyFile.toString());
        if (config.configUrl() != null && !config.configUrl().isBlank()) {
            command.add("--config-url");
            command.add(config.configUrl());
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logDir.resolve("node.out.log").toFile()));
        builder.redirectError(ProcessBuilder.Redirect.appendTo(logDir.resolve("node.err.log").toFile()));
        builder.environment().clear();
        builder.environment().put("PATH", System.getenv().getOrDefault("PATH", ""));
        builder.environment().put("NODE_ENV", "production");
        builder.environment().put("NEXUS_AI_HOME", config.dataDir().toString());
        config.environment().forEach((key, value) -> {
            if (key.startsWith("NEXUS_AI_")) {
                builder.environment().put(key, value);
            }
        });
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("cannot start node runtime", e);
        }
        long deadline = System.nanoTime() + config.startupTimeout().toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(readyFile)) {
                try {
                    String text = Files.readString(readyFile, StandardCharsets.UTF_8);
                    Matcher matcher = PORT.matcher(text);
                    if (matcher.find()) {
                        int port = Integer.parseInt(matcher.group(1));
                        Matcher version = VERSION.matcher(text);
                        if (version.find()) {
                            runtimeVersion = version.group(1);
                        }
                        Matcher configMatcher = CONFIG_VERSION.matcher(text);
                        if (configMatcher.find()) {
                            configVersion = configMatcher.group(1);
                        }
                        AiEndpoint candidate = new AiEndpoint(URI.create("http://127.0.0.1:" + port));
                        if (waitHealthy(candidate)) {
                            return candidate;
                        }
                    }
                } catch (IOException ignored) {
                    // retry
                }
            }
            if (!process.isAlive()) {
                throw new IllegalStateException("node process exited with code " + process.exitValue());
            }
            sleep(100);
        }
        process.destroyForcibly();
        throw new IllegalStateException("node runtime did not become ready in time");
    }

    private boolean waitHealthy(AiEndpoint candidate) {
        try {
            int status = checkHttp(candidate);
            return status == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private int checkHttp(AiEndpoint candidate) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(candidate.baseUri().resolve("/v1/health"))
                .timeout(config.healthTimeout())
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private void startMonitor() {
        monitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "nexus-ai-health");
            thread.setDaemon(true);
            return thread;
        });
        monitor.scheduleWithFixedDelay(this::monitorOnce,
                config.healthCheckInterval().toMillis(),
                config.healthCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private void monitorOnce() {
        AiEndpoint current = endpoint;
        if (current == null) {
            return;
        }
        long started = System.nanoTime();
        try {
            int status = checkHttp(current);
            long latency = (System.nanoTime() - started) / 1_000_000L;
            if (status == 200) {
                failures = 0;
                updateHealth(AiRuntimeStatus.RUNNING, latency, null);
            } else {
                failures++;
                degradedOrFailed(latency, "health status " + status);
            }
        } catch (Exception e) {
            failures++;
            degradedOrFailed(-1, e.getMessage());
        }
    }

    private void degradedOrFailed(long latency, String error) {
        if (failures >= config.unhealthyThreshold()) {
            updateHealth(AiRuntimeStatus.DEGRADED, latency, error);
        } else {
            updateHealth(health.get().state(), latency, error);
        }
    }

    private void updateHealth(AiRuntimeStatus state, long latency, String error) {
        health.set(new AiRuntimeHealth(state, health.get().since(), runtimeVersion, nodeVersion,
                process == null ? -1 : process.pid(), Instant.now(), latency, configVersion, error));
    }

    private void setState(AiRuntimeStatus state, String error) {
        health.set(new AiRuntimeHealth(state, Instant.now(), runtimeVersion, nodeVersion,
                process == null ? -1 : process.pid(), health.get().lastCheckAt(), -1, configVersion, error));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
