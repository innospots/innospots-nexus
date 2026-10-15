package com.innospots.nexus.sample.spring.platform.support;

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
 * 将指定表以 JSON 输出到日志与 {@code target/test-output}。
 */
public final class PlatformDatabaseJsonSnapshot {

    private static final Logger LOGGER = LogManager.getLogger(PlatformDatabaseJsonSnapshot.class);

    private PlatformDatabaseJsonSnapshot() {
    }

    public static void logAndPersist(
            JdbcTemplate jdbcTemplate,
            String outputDirectoryName,
            String stepLabel,
            String tableName,
            String orderByColumn) {
        logAndPersist(jdbcTemplate, outputDirectoryName, stepLabel, Map.of(tableName, orderByColumn));
    }

    public static void logAndPersist(
            JdbcTemplate jdbcTemplate,
            String outputDirectoryName,
            String stepLabel,
            Map<String, String> tableOrderBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("step", stepLabel);
        for (Map.Entry<String, String> entry : tableOrderBy.entrySet()) {
            payload.put(entry.getKey(), queryTable(jdbcTemplate, entry.getKey(), entry.getValue()));
        }

        String json = Jsons.toJson(payload);
        LOGGER.info("sample platform DB snapshot [{}] ({}): {}", outputDirectoryName, stepLabel, json);

        try {
            Path dumpDirectory = Path.of("target", "test-output", outputDirectoryName);
            Files.createDirectories(dumpDirectory);
            String fileName = stepLabel.replaceAll("[^a-zA-Z0-9_-]+", "_") + ".json";
            Files.writeString(dumpDirectory.resolve(fileName), json);
        } catch (Exception ex) {
            LOGGER.warn(
                    "Failed to write DB snapshot for {} step {}: {}",
                    outputDirectoryName,
                    stepLabel,
                    ex.toString());
        }
    }

    private static List<Map<String, Object>> queryTable(
            JdbcTemplate jdbcTemplate,
            String table,
            String orderBy) {
        return jdbcTemplate.queryForList("SELECT * FROM " + table + " ORDER BY " + orderBy);
    }
}
