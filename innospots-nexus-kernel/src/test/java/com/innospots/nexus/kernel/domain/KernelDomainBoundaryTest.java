package com.innospots.nexus.kernel.domain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KernelDomainBoundaryTest {

    @Test
    void kernelDefinesRequestedDomainBoundaries() {
        assertThat(KernelDomain.values())
                .extracting(KernelDomain::code)
                .containsExactlyInAnyOrder(
                        "auth",
                        "menu",
                        "audit-log",
                        "user",
                        "user-group",
                        "role",
                        "permission",
                        "dictionary",
                        "notification",
                        "system-config",
                        "i18n",
                        "category"
                );
    }

    @Test
    void kernelDomainSourcesAvoidConcreteWebFrameworkBindings() throws Exception {
        Path sourceRoot = Path.of("src/main/java");
        List<String> forbidden = List.of(
                "import org.springframework",
                "import io.quarkus",
                "import jakarta.ws.rs",
                "import jakarta.inject",
                "import jakarta.interceptor",
                "import jakarta.servlet"
        );

        List<Path> javaFiles;
        try (var stream = Files.walk(sourceRoot)) {
            javaFiles = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }

        for (Path javaFile : javaFiles) {
            String source = Files.readString(javaFile);
            assertThat(forbidden)
                    .withFailMessage("Forbidden framework dependency in %s", javaFile)
                    .noneMatch(source::contains);
        }
    }
}
