package com.innospots.nexus.sample.spring.platform.catalog;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.catalog.support.CatalogTreeVerificationSupport;
import com.innospots.nexus.sample.spring.platform.role.support.RoleEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成验证 {@code GET /catalog/permission-catalog} 树节点数量与 MODULE grants。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_CATALOG_TREE_OPS_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class ConsoleCatalogTreeOperationsIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleCatalogTreeOperationsIntegrationTest.class);

    private static final String CATALOG_TREE_PATH = ConsoleConstant.apiPath("/catalog/permission-catalog");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EndpointGrantManager endpointGrantManager;

    @Test
    void catalogTreeNodeCountMatchesEnabledModuleAndMenuIndex() throws Exception {
        List<CatalogNodeVo> tree = fetchCatalogTree();

        long moduleRows = countEnabledResources(CatalogResourceType.MODULE.name());
        long menuRows = countEnabledResources(CatalogResourceType.MENU.name());
        List<CatalogNodeVo> flat = CatalogTreeVerificationSupport.flattenNodes(tree);

        LOGGER.info(
                "Catalog tree nodes: roots={} flat={} dbModule={} dbMenu={} summary={}",
                tree.size(),
                flat.size(),
                moduleRows,
                menuRows,
                Jsons.toJson(CatalogTreeVerificationSupport.summarizeGrants(tree)));

        assertThat(tree).hasSize((int) moduleRows);
        assertThat(flat).hasSize((int) (moduleRows + menuRows));
    }

    @Test
    void catalogTreeModuleGrantsMatchOperableEndpointsPerModule() throws Exception {
        List<CatalogNodeVo> tree = fetchCatalogTree();
        Map<String, Set<String>> operableByModule =
                CatalogTreeVerificationSupport.operableGrantNamesByModule(endpointGrantManager);

        LOGGER.info("Operable grant names by module: {}", Jsons.toJson(operableByModule));

        for (CatalogNodeVo node : tree) {
            CatalogTreeVerificationSupport.assertModuleOperationGrantsMatchEndpointCatalog(
                    node, operableByModule);
        }
    }

    @Test
    void catalogModuleNodesExposeExpectedOperableSamples() throws Exception {
        List<CatalogNodeVo> tree = fetchCatalogTree();

        CatalogNodeVo dictionaryModule = tree.stream()
                .filter(node -> "dictionary".equals(node.moduleKey()))
                .findFirst()
                .orElseThrow();
        assertThat(dictionaryModule.grants()).isNotNull();
        assertThat(dictionaryModule.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .map(item -> item.grantName()))
                .contains("dictionary.type.create", "dictionary.type.delete")
                .doesNotContain("dictionary.type.page", "dictionary.type.get");

        CatalogNodeVo userModule = tree.stream()
                .filter(node -> "user".equals(node.moduleKey()))
                .findFirst()
                .orElseThrow();
        assertThat(userModule.grants().operationGroups().stream()
                .flatMap(group -> group.items().stream())
                .map(item -> item.grantName()))
                .contains("platform.user.create")
                .doesNotContain("platform.user.page");

        for (CatalogNodeVo node : tree) {
            CatalogTreeVerificationSupport.assertOperationGrantsExcludeReadOnlyActions(
                    node, endpointGrantManager);
        }

        LOGGER.info("dictionary operation grants: {}", dictionaryModule.grants().operationGroups());
        LOGGER.info("user operation grant count: {}",
                userModule.grants().operationGroups().stream().mapToInt(g -> g.items().size()).sum());
    }

    private List<CatalogNodeVo> fetchCatalogTree() throws Exception {
        RoleEndpointHttpClient http = new RoleEndpointHttpClient(port);
        HttpResponse<String> response = http.get(CATALOG_TREE_PATH);
        LOGGER.info("GET {} status={}", CATALOG_TREE_PATH, response.statusCode());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return CatalogTreeVerificationSupport.parseTreeResponse(response.body());
    }

    private long countEnabledResources(String resourceType) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM nx_console_catalog_resource
                WHERE status = 'ENABLED' AND resource_type = ?
                """,
                Long.class,
                resourceType);
        return count == null ? 0L : count;
    }
}
