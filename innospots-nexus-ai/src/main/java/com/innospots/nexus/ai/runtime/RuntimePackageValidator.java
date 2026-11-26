package com.innospots.nexus.ai.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验 npm 包名称、版本与 dist 入口。
 */
public final class RuntimePackageValidator {

    private static final Pattern VERSION = Pattern.compile("\"version\"\\s*:\\s*\"([^\"]+)\"");

    /**
     * 校验目录。
     *
     * @param runtimeDir npm 包根目录
     * @return 包版本
     */
    public String validate(Path runtimeDir) {
        Path packageJson = runtimeDir.resolve("package.json");
        Path entry = runtimeDir.resolve("dist").resolve("server.mjs");
        if (!Files.isRegularFile(packageJson)) {
            throw new IllegalStateException("runtime package.json missing: " + packageJson);
        }
        if (!Files.isRegularFile(entry)) {
            throw new IllegalStateException("runtime dist/server.mjs missing: " + entry);
        }
        String text;
        try {
            text = Files.readString(packageJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read runtime package.json", e);
        }
        if (!text.contains("\"@innospots/nexus-ai-runtime\"")) {
            throw new IllegalStateException("unexpected runtime package name");
        }
        Matcher matcher = VERSION.matcher(text);
        if (!matcher.find()) {
            throw new IllegalStateException("runtime package version missing");
        }
        String version = matcher.group(1);
        String expected = expectedVersion();
        if (expected != null && !expected.equals(version)) {
            throw new IllegalStateException("runtime version mismatch: expected " + expected + ", found " + version);
        }
        return version;
    }

    /**
     * 从自身 jar manifest 读取期望版本。
     *
     * @return 版本或 null
     */
    public String expectedVersion() {
        String property = System.getProperty("nexus.ai.runtime.version");
        if (property != null && !property.isBlank()) {
            return property;
        }
        try {
            Path location = Path.of(RuntimePackageValidator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location)) {
                try (JarFile jar = new JarFile(location.toFile())) {
                    Manifest manifest = jar.getManifest();
                    Attributes attributes = manifest == null ? null : manifest.getMainAttributes();
                    String value = attributes == null ? null : attributes.getValue("Nexus-Ai-Runtime-Version");
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }
}
