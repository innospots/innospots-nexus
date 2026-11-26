package com.innospots.nexus.ai.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Node 版本校验。
 */
public final class NodeVersionChecker {

    private static final int MIN_MAJOR = 22;
    private static final int MIN_MINOR = 19;

    /**
     * 校验 node 可执行文件版本。
     *
     * @param nodeExecutable node 命令
     * @return 版本字符串
     */
    public String check(String nodeExecutable) {
        try {
            Process process = new ProcessBuilder(nodeExecutable, "--version").redirectErrorStream(true).start();
            String version;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                version = reader.readLine();
            }
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("node --version timed out");
            }
            if (version == null || !version.startsWith("v")) {
                throw new IllegalStateException("unexpected node version: " + version);
            }
            String[] parts = version.substring(1).split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            if (major < MIN_MAJOR || (major == MIN_MAJOR && minor < MIN_MINOR)) {
                throw new IllegalStateException("Node >= " + MIN_MAJOR + "." + MIN_MINOR + " required, found " + version);
            }
            return version;
        } catch (IOException | InterruptedException | NumberFormatException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("node is not available: " + e.getMessage(), e);
        }
    }
}
