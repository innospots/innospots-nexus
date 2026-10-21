package com.innospots.nexus.sample.spring.platform.catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.catalog.support.CatalogDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 console catalog 启动同步写入 {@code nx_console_catalog_resource} 的实际数据：
 * 统计行数与分类数量、逐条明细，并把结果以 JSON 输出到日志与 {@code target/test-output}。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_CATALOG_SYNC_DB_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class ConsoleCatalogStartupSyncDatabaseIntegrationTest {

    private static final Logger LOGGER =
            LogManager.getLogger(ConsoleCatalogStartupSyncDatabaseIntegrationTest.class);

    private static final String PLATFORM_CONSOLE_PLUGIN = BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE;

    private static final Set<String> EXPECTED_PLATFORM_MODULE_KEYS = Set.of(
            "organization",
            "menu",
            "user",
            "dictionary",
            "role",
            "permission",
            "logger",
            "settings");

    private static final String SELECT_ALL = """
            SELECT resource_id, owner_plugin_id, domain_key, module_key, resource_type, resource_key,
                   parent_resource_id, entry_page_key, display_name, description, sort_order, icon,
                   view_groups, operation_groups, status, security_realm, created_at, updated_at
            FROM nx_console_catalog_resource
            ORDER BY resource_type, module_key, sort_order, resource_key
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConsoleCatalogSyncService catalogSyncService;

    @Test
    void startupSyncPersistsCatalogRowsAndLogsJsonReport() {
        List<Map<String, Object>> rows = loadRows();
        Map<String, Object> report = buildReport(rows);

        String reportJson = Jsons.toJson(report);
        LOGGER.info("console catalog startup sync DB report: {}", reportJson);
        CatalogDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "startup-sync-database");

        List<Map<String, Object>> modules = rowsOfType(rows, CatalogResourceType.MODULE);
        List<Map<String, Object>> menus = rowsOfType(rows, CatalogResourceType.MENU);
        List<Map<String, Object>> platformRows = rows.stream()
                .filter(row -> PLATFORM_CONSOLE_PLUGIN.equals(row.get("ownerPluginId")))
                .toList();
        List<Map<String, Object>> platformModules = rowsOfType(platformRows, CatalogResourceType.MODULE);
        List<Map<String, Object>> platformMenus = rowsOfType(platformRows, CatalogResourceType.MENU);

        assertThat(rows).as("startup console-catalog-sync must write catalog rows").isNotEmpty();
        assertThat(rows).allSatisfy(row -> assertThat(row.get("status"))
                .isEqualTo(BasicStatus.ENABLED.name()));
        assertThat(rows.stream().map(row -> row.get("resourceType")).distinct())
                .contains(CatalogResourceType.MODULE.name());
        assertThat(rows).allSatisfy(row -> assertThat(row.get("securityRealm"))
                .as("%s security realm", row.get("resourceKey"))
                .isIn("PLATFORM", "TENANT"));
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.get("resourceId")).as("resourceId").isNotNull();
            assertThat(row.get("resourceKey")).as("resourceKey").isNotNull();
            assertThat(row.get("domainKey")).as("%s domainKey", row.get("resourceKey")).isNotNull();
            assertThat(row.get("moduleKey")).as("%s moduleKey", row.get("resourceKey")).isNotNull();
            assertThat(row.get("displayName")).as("%s displayName", row.get("resourceKey"))
                    .isNotNull();
            assertThat(row.get("createdAt")).as("%s createdAt", row.get("resourceKey")).isNotNull();
        });

        assertThat(distinctCount(rows, "resourceId"))
                .as("no duplicated catalog resource ids")
                .isEqualTo(rows.size());
        assertThat(distinctCount(rows, "resourceKey"))
                .as("no duplicated catalog resource keys")
                .isEqualTo(rows.size());

        assertThat(platformRows).allSatisfy(row -> assertThat(row.get("securityRealm"))
                .as("%s platform security realm", row.get("resourceKey"))
                .isEqualTo("PLATFORM"));
        assertThat(platformModules).hasSize(EXPECTED_PLATFORM_MODULE_KEYS.size());
        assertThat(platformModules.stream().map(row -> row.get("moduleKey")))
                .containsExactlyInAnyOrderElementsOf(EXPECTED_PLATFORM_MODULE_KEYS);
        assertThat(platformMenus)
                .as("single-page platform modules must not contribute MENU rows")
                .isEmpty();
        assertThat(platformModules).allSatisfy(row -> {
            assertThat(row.get("entryPageKey"))
                    .as("%s entryPageKey", row.get("resourceKey"))
                    .isNotNull();
            assertThat(row.get("viewGroups"))
                    .as("%s viewGroups", row.get("resourceKey"))
                    .isNotNull();
            assertThat(row.get("operationGroups"))
                    .as("%s operationGroups", row.get("resourceKey"))
                    .isNotNull();
        });

        assertParentLinksAreValid(platformModules, platformMenus);

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) report.get("summary");
        assertThat(summary.get("totalRows")).isEqualTo((long) rows.size());
        assertThat(summary.get("moduleRows")).isEqualTo((long) modules.size());
        assertThat(summary.get("menuRows")).isEqualTo((long) menus.size());
        assertThat(summary.get("disabledRows")).isEqualTo(0L);

        LOGGER.info(
                "console catalog DB totals: total={} modules={} menus={} platformModules={} platformMenus={}",
                rows.size(),
                modules.size(),
                menus.size(),
                platformModules.size(),
                platformMenus.size());
    }

    @Test
    void repeatedSyncWritesNoNewRowsAndDisablesUnknownRows() {
        long baseline = countAll();

        CatalogSyncResult repeated = catalogSyncService.sync();
        LOGGER.info("repeated catalog sync result: {}", Jsons.toJson(repeated));

        assertThat(repeated.createdResources()).as("repeated sync must not insert").isZero();
        assertThat(repeated.updatedResources()).as("repeated sync must not rewrite").isZero();
        assertThat(repeated.disabledResources()).as("repeated sync must not disable").isZero();
        assertThat(countAll()).isEqualTo(baseline);

        String sentinelId = "itsentinel0000000000000000000001";
        String sentinelKey = "it:sentinel-module";
        jdbcTemplate.update("""
                INSERT INTO nx_console_catalog_resource (
                    resource_id, owner_plugin_id, domain_key, module_key, resource_type, resource_key,
                    sort_order, status, security_realm, created_at, updated_at
                ) VALUES (?, ?, 'it', 'it-sentinel', 'MODULE', ?, 999, 'ENABLED', 'PLATFORM',
                          CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                sentinelId,
                PLATFORM_CONSOLE_PLUGIN,
                sentinelKey);

        try {
            CatalogSyncResult withSentinel = catalogSyncService.sync();
            LOGGER.info("catalog sync with unknown row result: {}", Jsons.toJson(withSentinel));

            assertThat(withSentinel.createdResources()).isZero();
            assertThat(withSentinel.disabledResources()).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT status FROM nx_console_catalog_resource WHERE resource_id = ?",
                    String.class,
                    sentinelId))
                    .isEqualTo(BasicStatus.DISABLED.name());
            assertThat(countAll()).isEqualTo(baseline + 1);
        } finally {
            jdbcTemplate.update(
                    "DELETE FROM nx_console_catalog_resource WHERE resource_id = ?", sentinelId);
        }
    }

    private List<Map<String, Object>> loadRows() {
        List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(SELECT_ALL);
        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (Map<String, Object> raw : rawRows) {
            rows.add(toCamelCaseRow(raw));
        }
        return rows;
    }

    private Map<String, Object> toCamelCaseRow(Map<String, Object> raw) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("resourceId", raw.get("resource_id"));
        row.put("ownerPluginId", raw.get("owner_plugin_id"));
        row.put("domainKey", raw.get("domain_key"));
        row.put("moduleKey", raw.get("module_key"));
        row.put("resourceType", raw.get("resource_type"));
        row.put("resourceKey", raw.get("resource_key"));
        row.put("parentResourceId", raw.get("parent_resource_id"));
        row.put("entryPageKey", raw.get("entry_page_key"));
        row.put("displayName", raw.get("display_name"));
        row.put("description", raw.get("description"));
        row.put("sortOrder", raw.get("sort_order"));
        row.put("icon", raw.get("icon"));
        row.put("viewGroups", raw.get("view_groups"));
        row.put("operationGroups", raw.get("operation_groups"));
        row.put("status", raw.get("status"));
        row.put("securityRealm", raw.get("security_realm"));
        row.put("createdAt", raw.get("created_at"));
        row.put("updatedAt", raw.get("updated_at"));
        return row;
    }

    private Map<String, Object> buildReport(List<Map<String, Object>> rows) {
        List<Map<String, Object>> modules = rowsOfType(rows, CatalogResourceType.MODULE);
        List<Map<String, Object>> menus = rowsOfType(rows, CatalogResourceType.MENU);
        Map<String, String> resourceKeyById = rows.stream().collect(Collectors.toMap(
                row -> String.valueOf(row.get("resourceId")),
                row -> String.valueOf(row.get("resourceKey")),
                (left, right) -> left,
                LinkedHashMap::new));
        Map<String, Long> menuCountByModule = menus.stream().collect(Collectors.groupingBy(
                row -> String.valueOf(row.get("moduleKey")),
                LinkedHashMap::new,
                Collectors.counting()));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRows", (long) rows.size());
        summary.put("enabledRows", countByStatus(rows, BasicStatus.ENABLED.name()));
        summary.put("disabledRows", countByStatus(rows, BasicStatus.DISABLED.name()));
        summary.put("moduleRows", (long) modules.size());
        summary.put("menuRows", (long) menus.size());
        summary.put("ownerPluginIds", distinctValues(rows, "ownerPluginId"));
        summary.put("moduleKeys", modules.stream()
                .map(row -> row.get("moduleKey"))
                .distinct()
                .toList());
        summary.put("securityRealms", distinctValues(rows, "securityRealm"));
        summary.put("menuCountByModule", menuCountByModule);

        List<Map<String, Object>> moduleDetails = modules.stream()
                .map(row -> detail(row, resourceKeyById, menuCountByModule))
                .toList();
        List<Map<String, Object>> menuDetails = menus.stream()
                .map(row -> detail(row, resourceKeyById, menuCountByModule))
                .toList();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("summary", summary);
        report.put("modules", moduleDetails);
        report.put("menus", menuDetails);
        return report;
    }

    private Map<String, Object> detail(
            Map<String, Object> row,
            Map<String, String> resourceKeyById,
            Map<String, Long> menuCountByModule) {
        Map<String, Object> detail = new LinkedHashMap<>(row);
        Object parentId = row.get("parentResourceId");
        detail.put("parentResourceKey",
                parentId == null ? null : resourceKeyById.get(String.valueOf(parentId)));
        if (CatalogResourceType.MODULE.name().equals(row.get("resourceType"))) {
            detail.put("childMenuCount",
                    menuCountByModule.getOrDefault(String.valueOf(row.get("moduleKey")), 0L));
        }
        return detail;
    }

    private void assertParentLinksAreValid(
            List<Map<String, Object>> modules,
            List<Map<String, Object>> menus) {
        Map<String, Map<String, Object>> moduleById = modules.stream().collect(Collectors.toMap(
                row -> String.valueOf(row.get("resourceId")),
                row -> row,
                (left, right) -> left,
                LinkedHashMap::new));

        assertThat(modules).allSatisfy(module -> assertThat(module.get("parentResourceId"))
                .as("top-level module %s must not have a parent", module.get("resourceKey"))
                .isNull());

        for (Map<String, Object> menu : menus) {
            Object parentId = menu.get("parentResourceId");
            assertThat(parentId)
                    .as("menu %s must point to its module", menu.get("resourceKey"))
                    .isNotNull();
            Map<String, Object> parent = moduleById.get(String.valueOf(parentId));
            assertThat(parent)
                    .as("menu %s parent must be a MODULE of the same plugin",
                            menu.get("resourceKey"))
                    .isNotNull();
            assertThat(menu.get("moduleKey"))
                    .as("menu %s module key must match parent", menu.get("resourceKey"))
                    .isEqualTo(parent.get("moduleKey"));
            assertThat(menu.get("viewGroups"))
                    .as("menu %s view groups JSON", menu.get("resourceKey"))
                    .isNotNull();
            assertThat(menu.get("operationGroups"))
                    .as("menu %s operation groups JSON", menu.get("resourceKey"))
                    .isNotNull();
        }
    }

    private List<Map<String, Object>> rowsOfType(
            List<Map<String, Object>> rows,
            CatalogResourceType type) {
        return rows.stream()
                .filter(row -> type.name().equals(row.get("resourceType")))
                .toList();
    }

    private long countByStatus(List<Map<String, Object>> rows, String status) {
        return rows.stream().filter(row -> status.equals(row.get("status"))).count();
    }

    private long distinctCount(List<Map<String, Object>> rows, String column) {
        return rows.stream().map(row -> row.get(column)).distinct().count();
    }

    private Set<Object> distinctValues(List<Map<String, Object>> rows, String column) {
        return new LinkedHashSet<>(rows.stream().map(row -> row.get(column)).toList());
    }

    private long countAll() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_console_catalog_resource", Long.class);
        return count == null ? 0L : count;
    }
}
