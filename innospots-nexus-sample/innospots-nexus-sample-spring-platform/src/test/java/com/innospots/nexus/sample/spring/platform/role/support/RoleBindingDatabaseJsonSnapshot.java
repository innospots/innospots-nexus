package com.innospots.nexus.sample.spring.platform.role.support;

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
 * 将 {@code nx_role_binding}（及关联 {@code nx_role}）以 JSON 输出到日志与文件。
 */
public final class RoleBindingDatabaseJsonSnapshot {

    private static final Logger LOGGER = LogManager.getLogger(RoleBindingDatabaseJsonSnapshot.class);

    private static final Path DUMP_DIRECTORY =
            Path.of("target", "test-output", "sample-platform-role-binding");

    private RoleBindingDatabaseJsonSnapshot() {
    }

    public static void logAndPersist(JdbcTemplate jdbcTemplate, String stepLabel) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("step", stepLabel);
        payload.put("nx_role", queryTable(jdbcTemplate, "nx_role", "role_code"));
        payload.put("nx_role_binding", queryTable(jdbcTemplate, "nx_role_binding", "binding_id"));

        String json = Jsons.toJson(payload);
        LOGGER.info("sample platform role binding DB snapshot [{}]: {}", stepLabel, json);

        try {
            Files.createDirectories(DUMP_DIRECTORY);
            String fileName = stepLabel.replaceAll("[^a-zA-Z0-9_-]+", "_") + ".json";
            Files.writeString(DUMP_DIRECTORY.resolve(fileName), json);
        } catch (Exception ex) {
            LOGGER.warn("Failed to write role binding DB snapshot for step {}: {}", stepLabel, ex.toString());
        }
    }

    private static List<Map<String, Object>> queryTable(JdbcTemplate jdbcTemplate, String table, String orderBy) {
        return jdbcTemplate.queryForList("SELECT * FROM " + table + " ORDER BY " + orderBy);
    }
}
