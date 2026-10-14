package com.innospots.nexus.sample.spring.platform.catalog.support;

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
 * 将 {@code nx_console_catalog_resource} 以 JSON 输出到日志与文件。
 */
public final class CatalogDatabaseJsonSnapshot {

    private static final Logger LOGGER = LogManager.getLogger(CatalogDatabaseJsonSnapshot.class);

    private static final Path DUMP_DIRECTORY =
            Path.of("target", "test-output", "sample-platform-catalog-sync");

    private CatalogDatabaseJsonSnapshot() {
    }

    public static void logAndPersist(JdbcTemplate jdbcTemplate, String stepLabel) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT resource_id, owner_plugin_id, module_key, resource_type, resource_key,
                       parent_resource_id, page_key, route_path, display_name, sort_order, status, security_realm
                FROM nx_console_catalog_resource
                ORDER BY resource_key
                """);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("step", stepLabel);
        payload.put("nx_console_catalog_resource", rows);

        String json = Jsons.toJson(payload);
        LOGGER.info("sample platform catalog DB snapshot [{}]: {}", stepLabel, json);

        try {
            Files.createDirectories(DUMP_DIRECTORY);
            String fileName = stepLabel.replaceAll("[^a-zA-Z0-9_-]+", "_") + ".json";
            Files.writeString(DUMP_DIRECTORY.resolve(fileName), json);
        } catch (Exception ex) {
            LOGGER.warn("Failed to write catalog DB snapshot for step {}: {}", stepLabel, ex.toString());
        }
    }
}
