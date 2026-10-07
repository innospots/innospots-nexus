package com.innospots.nexus.spring.console.role.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 为集成测试配置 H2 文件库（非内存），目录位于 {@code target/h2/console-role-it}。
 */
public abstract class RoleEndpointH2Support {

    private static final Path DATABASE_DIRECTORY = prepareDatabaseDirectory();

    private static Path prepareDatabaseDirectory() {
        try {
            Path directory = Path.of("target", "h2", "console-role-it", UUID.randomUUID().toString());
            Files.createDirectories(directory);
            return directory.toAbsolutePath();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to prepare H2 file database directory", ex);
        }
    }

    @DynamicPropertySource
    static void registerFileDatasource(DynamicPropertyRegistry registry) {
        String jdbcUrl = "jdbc:h2:file:" + DATABASE_DIRECTORY + "/nexus"
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
        registry.add("spring.datasource.url", () -> jdbcUrl);
    }
}
