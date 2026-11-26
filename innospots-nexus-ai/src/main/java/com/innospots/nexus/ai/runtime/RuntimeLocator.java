package com.innospots.nexus.ai.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 定位 npm runtime 包根目录。
 */
public final class RuntimeLocator {

    /**
     * 按显式配置、环境变量、node_modules、ai/runtime 顺序定位。
     *
     * @param configured 显式目录，可为 null
     * @return npm 包根目录
     */
    public Path locate(Path configured) {
        if (configured != null) {
            return configured;
        }
        String env = System.getenv("NEXUS_AI_RUNTIME_DIR");
        if (env != null && !env.isBlank()) {
            return Paths.get(env);
        }
        Path appDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path nodeModules = appDir.resolve("node_modules").resolve("@innospots").resolve("nexus-ai-runtime");
        if (Files.isDirectory(nodeModules)) {
            return nodeModules;
        }
        Path legacy = appDir.resolve("ai").resolve("runtime");
        if (Files.isDirectory(legacy)) {
            return legacy;
        }
        throw new IllegalStateException("runtime package not found: " + nodeModules);
    }
}
