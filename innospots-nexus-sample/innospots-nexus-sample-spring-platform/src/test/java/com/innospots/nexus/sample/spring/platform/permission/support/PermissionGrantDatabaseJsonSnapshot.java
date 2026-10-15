package com.innospots.nexus.sample.spring.platform.permission.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innospots.nexus.base.json.Jsons;

/**
 * 将 {@code nx_permission_grant} 及关联目录资源以 JSON 输出到日志与文件。
 */
public final class PermissionGrantDatabaseJsonSnapshot {

    private static final Logger LOGGER = LogManager.getLogger(PermissionGrantDatabaseJsonSnapshot.class);

    private static final Path DUMP_DIRECTORY =
            Path.of("target", "test-output", "sample-platform-permission-grant");

    private PermissionGrantDatabaseJsonSnapshot() {
    }

    public static void logAndPersist(JdbcTemplate jdbcTemplate, String stepLabel) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("step", stepLabel);
        payload.put("nx_permission_grant", queryGrantsWithResourceKeys(jdbcTemplate));

        String json = Jsons.toJson(payload);
        LOGGER.info("sample platform permission grant DB snapshot [{}]: {}", stepLabel, json);

        try {
            Files.createDirectories(DUMP_DIRECTORY);
            String fileName = stepLabel.replaceAll("[^a-zA-Z0-9_-]+", "_") + ".json";
            Files.writeString(DUMP_DIRECTORY.resolve(fileName), json);
        } catch (Exception ex) {
            LOGGER.warn("Failed to write permission grant DB snapshot for step {}: {}", stepLabel, ex.toString());
        }
    }

    private static List<Map<String, Object>> queryGrantsWithResourceKeys(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList(
                """
                SELECT g.*, r.resource_key, r.resource_type
                FROM nx_permission_grant g
                LEFT JOIN nx_console_catalog_resource r ON g.resource_id = r.resource_id
                ORDER BY g.subject_id, r.resource_key
                """);
    }
}
