package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.ai.runtime.NodeVersionChecker;
import com.innospots.nexus.ai.runtime.RuntimeLocator;
import com.innospots.nexus.ai.runtime.RuntimePackageValidator;

class RuntimeChecksTest {

    @Test
    void validatesPackageAndVersion() throws Exception {
        Path dir = Files.createTempDirectory("nexus-ai-runtime");
        Files.writeString(dir.resolve("package.json"),
                "{\"name\":\"@innospots/nexus-ai-runtime\",\"version\":\"1.2.3\"}");
        Files.createDirectories(dir.resolve("dist"));
        Files.writeString(dir.resolve("dist/server.mjs"), "// runtime");
        System.setProperty("nexus.ai.runtime.version", "1.2.3");
        try {
            assertThat(new RuntimePackageValidator().validate(dir)).isEqualTo("1.2.3");
        } finally {
            System.clearProperty("nexus.ai.runtime.version");
        }
    }

    @Test
    void rejectsVersionMismatch() throws Exception {
        Path dir = Files.createTempDirectory("nexus-ai-runtime");
        Files.writeString(dir.resolve("package.json"),
                "{\"name\":\"@innospots/nexus-ai-runtime\",\"version\":\"1.2.3\"}");
        Files.createDirectories(dir.resolve("dist"));
        Files.writeString(dir.resolve("dist/server.mjs"), "// runtime");
        System.setProperty("nexus.ai.runtime.version", "9.9.9");
        try {
            assertThatThrownBy(() -> new RuntimePackageValidator().validate(dir))
                    .hasMessageContaining("version mismatch");
        } finally {
            System.clearProperty("nexus.ai.runtime.version");
        }
    }

    @Test
    void checksNodeVersion() {
        String version = new NodeVersionChecker().check("node");
        assertThat(version).startsWith("v");
    }

    @Test
    void locatorUsesExplicitDirectory() {
        Path dir = Path.of("/tmp/runtime");
        assertThat(new RuntimeLocator().locate(dir)).isEqualTo(dir);
    }
}
