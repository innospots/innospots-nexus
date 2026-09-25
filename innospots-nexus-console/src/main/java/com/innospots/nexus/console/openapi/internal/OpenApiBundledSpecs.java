package com.innospots.nexus.console.openapi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 读取 {@code META-INF/nexus-openapi/<specId>.yaml} 构建期产物。
 */
public final class OpenApiBundledSpecs {

    public static final String RESOURCE_ROOT = "META-INF/nexus-openapi";

    private static final String YAML_SUFFIX = ".yaml";

    private OpenApiBundledSpecs() {
    }

    public static List<String> listSpecIds(ClassLoader classLoader) {
        try {
            Set<String> specIds = new LinkedHashSet<>();
            Enumeration<URL> roots = classLoader.getResources(RESOURCE_ROOT);
            while (roots.hasMoreElements()) {
                collectSpecIds(roots.nextElement(), specIds);
            }
            List<String> ids = new ArrayList<>(specIds);
            Collections.sort(ids);
            return ids;
        } catch (IOException exception) {
            throw NexusException.build(
                    NexusStatusCode.SYSTEM_ERROR.fullCode(),
                    "Failed to scan OpenAPI specs",
                    exception);
        }
    }

    public static String readYaml(ClassLoader classLoader, String specId) {
        requireValidSpecId(specId);
        String path = RESOURCE_ROOT + "/" + specId + YAML_SUFFIX;
        try (InputStream input = classLoader.getResourceAsStream(path)) {
            if (input == null) {
                throw NexusException.build(
                        NexusStatusCode.RESOURCE_NOT_FOUND.fullCode(),
                        "OpenAPI spec not found: " + specId);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw NexusException.build(
                    NexusStatusCode.SYSTEM_ERROR.fullCode(),
                    "Failed to read OpenAPI spec: " + specId,
                    exception);
        }
    }

    private static void collectSpecIds(URL root, Set<String> specIds) throws IOException {
        if ("file".equals(root.getProtocol())) {
            Path directory;
            try {
                directory = Path.of(root.toURI());
            } catch (Exception exception) {
                throw NexusException.build(
                        NexusStatusCode.SYSTEM_ERROR.fullCode(),
                        "Failed to read OpenAPI resource directory",
                        exception);
            }
            if (!Files.isDirectory(directory)) {
                return;
            }
            try (var stream = Files.list(directory)) {
                stream.map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(YAML_SUFFIX))
                        .map(name -> name.substring(0, name.length() - YAML_SUFFIX.length()))
                        .forEach(specIds::add);
            }
            return;
        }
        if ("jar".equals(root.getProtocol())) {
            JarURLConnection connection = (JarURLConnection) root.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                String prefix = connection.getEntryName();
                if (prefix == null) {
                    return;
                }
                if (!prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith(prefix) || entry.isDirectory()) {
                        continue;
                    }
                    String fileName = name.substring(prefix.length());
                    if (!fileName.endsWith(YAML_SUFFIX) || fileName.contains("/")) {
                        continue;
                    }
                    specIds.add(fileName.substring(0, fileName.length() - YAML_SUFFIX.length()));
                }
            }
        }
    }

    private static void requireValidSpecId(String specId) {
        if (specId == null || specId.isBlank() || specId.contains("/") || specId.contains("..")) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "Invalid OpenAPI specId");
        }
    }
}
