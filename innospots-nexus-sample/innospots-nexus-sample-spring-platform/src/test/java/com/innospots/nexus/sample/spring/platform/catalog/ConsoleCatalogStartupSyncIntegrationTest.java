package com.innospots.nexus.sample.spring.platform.catalog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.catalog.support.CatalogDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.catalog.support.CatalogTreeJsonSupport;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证应用启动后插件宿主与目录同步启动任务已执行：查询 HTTP 目录树、库表数据，并以 JSON 输出比对。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:nexus-sample-platform-catalog-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.config.import=optional:classpath:nexus/console-auth.yaml,optional:classpath:nexus/console-credential.yaml,optional:classpath:nexus/console-web.yaml,optional:classpath:nexus/console-openapi.yaml,optional:classpath:nexus/console-plugins.yaml"
})
class ConsoleCatalogStartupSyncIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleCatalogStartupSyncIntegrationTest.class);

    private static final String CATALOG_TREE_PATH = ConsoleConstant.apiPath("/catalog/tree");

    private static final String PLATFORM_CONSOLE_PLUGIN = BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void startupSyncPopulatesCatalogIndexMatchingTreeEndpoint() throws Exception {
        LOGGER.info(
                "Startup log markers to verify in test output: "
                        + "Nexus startup running task name=plugin-host, "
                        + "Enabling plugin host, Plugin host enabled, "
                        + "Nexus startup running task name=console-catalog-sync, "
                        + "Console catalog sync startup task finished created=");

        Long pluginRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_plugin_installation WHERE plugin_id = ?",
                Long.class,
                PLATFORM_CONSOLE_PLUGIN);
        assertThat(pluginRows)
                .as("plugin-host startup should register platform console plugin installation")
                .isGreaterThanOrEqualTo(1L);

        CatalogDatabaseJsonSnapshot.logAndPersist(jdbcTemplate, "after-startup-sync");

        Long enabledCatalogRows = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_console_catalog_resource
                WHERE status = 'ENABLED' AND owner_plugin_id = ?
                """,
                Long.class,
                PLATFORM_CONSOLE_PLUGIN);
        assertThat(enabledCatalogRows)
                .as("console-catalog-sync startup should index ACTIVE plugin contributions")
                .isGreaterThan(0L);

        RoleEndpointHttpClient http = new RoleEndpointHttpClient(port);
        HttpResponse<String> treeResponse = http.get(CATALOG_TREE_PATH);
        LOGGER.info("GET {} status={} body={}", CATALOG_TREE_PATH, treeResponse.statusCode(), treeResponse.body());
        assertThat(treeResponse.statusCode()).as(treeResponse.body()).isEqualTo(200);
        assertThat(treeResponse.body()).contains("\"success\":true");

        Set<String> apiResourceKeys =
                CatalogTreeJsonSupport.extractResourceKeys(treeResponse.body(), PLATFORM_CONSOLE_PLUGIN);
        List<String> apiModuleKeys =
                CatalogTreeJsonSupport.extractModuleKeys(treeResponse.body(), PLATFORM_CONSOLE_PLUGIN);
        assertThat(apiModuleKeys).containsExactlyInAnyOrder(
                "organization",
                "menu",
                "user",
                "dictionary",
                "role",
                "permission",
                "logger",
                "settings");
        assertThat(apiResourceKeys).contains(
                "module:organization",
                "module:user",
                "module:settings",
                "module:role",
                "module:dictionary");

        List<String> dbResourceKeyList = jdbcTemplate.queryForList(
                """
                SELECT resource_key FROM nx_console_catalog_resource
                WHERE status = 'ENABLED' AND owner_plugin_id = ?
                ORDER BY resource_key
                """,
                String.class,
                PLATFORM_CONSOLE_PLUGIN);
        Set<String> dbResourceKeys = new LinkedHashSet<>(dbResourceKeyList);

        Map<String, Object> verification = new LinkedHashMap<>();
        verification.put("ownerPluginId", PLATFORM_CONSOLE_PLUGIN);
        verification.put("apiModuleKeys", apiModuleKeys);
        verification.put("apiResourceKeyCount", apiResourceKeys.size());
        verification.put("dbResourceKeyCount", dbResourceKeys.size());
        verification.put("apiResourceKeys", apiResourceKeys);
        verification.put("dbResourceKeys", dbResourceKeys);
        verification.put("apiKeysSubsetOfDb", dbResourceKeys.containsAll(apiResourceKeys));
        verification.put("dbMenuResourceCount", countResourcesByType("MENU"));
        verification.put("dbActionResourceCount", countResourcesByType("ACTION"));
        verification.put("dbDatasourceResourceCount", countResourcesByType("DATASOURCE"));

        String verificationJson = Jsons.toJson(verification);
        LOGGER.info("catalog startup sync verification: {}", verificationJson);

        assertThat(dbResourceKeys)
                .as("GET /catalog/tree keys should be indexed in nx_console_catalog_resource")
                .containsAll(apiResourceKeys);
        assertThat(countResourcesByType("MENU")).isGreaterThan(0L);
        assertThat(countResourcesByType("PAGE")).isGreaterThanOrEqualTo(8L);
        assertThat(countResourcesByType("MODULE")).isEqualTo(8L);
    }

    private Long countResourcesByType(String resourceType) {
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_console_catalog_resource
                WHERE status = 'ENABLED' AND owner_plugin_id = ? AND resource_type = ?
                """,
                Long.class,
                PLATFORM_CONSOLE_PLUGIN,
                resourceType);
    }
}
