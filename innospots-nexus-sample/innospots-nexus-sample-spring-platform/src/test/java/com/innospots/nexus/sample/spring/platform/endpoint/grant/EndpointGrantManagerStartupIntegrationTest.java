package com.innospots.nexus.sample.spring.platform.endpoint.grant;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.console.catalog.endpoint.ConsoleCatalogEndpoint;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryTypeEndpoint;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint;
import com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.endpoint.grant.support.EndpointGrantManagerStartupExpectations;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 启动 sample 运营平台后，验证 {@link EndpointGrantManager} 已从 Spring 容器中的 JAX-RS 资源
 * 正确加载 grant 元数据（含 platform 契约实现、platform 自有 endpoint、console 模块实现类）。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_ENDPOINT_GRANT_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class EndpointGrantManagerStartupIntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(EndpointGrantManagerStartupIntegrationTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EndpointGrantManager endpointGrantManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void h2StartupAndPluginHostReady() {
        Long pluginRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nx_plugin_installation WHERE plugin_id = ?",
                Long.class,
                BuiltinPlatformEntryPlugins.PLATFORM_CONSOLE);
        assertThat(pluginRows).isGreaterThanOrEqualTo(1L);
        LOGGER.info("H2 plugin installation rows for platform console: {}", pluginRows);
    }

    @Test
    void startupRegistersAllGrantResourceTypesFromContext() {
        Set<Class<?>> resourceTypes = EndpointGrantManagerStartupExpectations.collectGrantResourceTypes(
                applicationContext);

        LOGGER.info("Grant resource types discovered: {}", resourceTypes);

        assertThat(resourceTypes)
                .containsAll(EndpointGrantManagerStartupExpectations.REQUIRED_RESOURCE_TYPES);
        assertThat(resourceTypes).contains(PlatformDictionaryTypeEndpoint.class);
        assertThat(resourceTypes).contains(PlatformUserEndpoint.class);
        assertThat(resourceTypes).contains(ConsoleCatalogEndpoint.class);
        assertThat(resourceTypes).contains(PluginManagementEndpoint.class);
        assertThat(resourceTypes).contains(PlatformInviteEndpoint.class);
    }

    @Test
    void startupLoadsMinimumDescriptorVolume() {
        long total = EndpointGrantManagerStartupExpectations.totalDescriptorCount(
                endpointGrantManager.allModuleCatalogs());
        LOGGER.info("Total registered grant descriptors after startup: {}", total);
        assertThat(total).isGreaterThanOrEqualTo(40L);
        assertThat(endpointGrantManager.allModuleCatalogs().keySet())
                .contains("dictionary", "user", "role", "catalog", "plugin", "auth");
    }

    @Test
    void startupManagerCatalogMatchesIndependentScan() {
        Map<String, EndpointModuleGrantCatalogVo> expected =
                EndpointGrantManagerStartupExpectations.computeExpectedCatalogs(applicationContext);
        Map<String, EndpointModuleGrantCatalogVo> actual = endpointGrantManager.allModuleCatalogs();

        LOGGER.info("Expected grant catalog summary:\n{}", String.join(
                "\n",
                EndpointGrantManagerStartupExpectations.formatCatalogSummary(expected)));
        LOGGER.info("Actual grant catalog summary:\n{}", String.join(
                "\n",
                EndpointGrantManagerStartupExpectations.formatCatalogSummary(actual)));

        assertThat(actual.keySet()).isEqualTo(expected.keySet());

        Set<String> expectedKeys = EndpointGrantManagerStartupExpectations.allDescriptorKeys(expected);
        Set<String> actualKeys = EndpointGrantManagerStartupExpectations.allDescriptorKeys(actual);

        LOGGER.info("Total descriptor keys expected={} actual={}", expectedKeys.size(), actualKeys.size());
        LOGGER.info("Module descriptor counts expected={}", EndpointGrantManagerStartupExpectations.countDescriptorsByModule(expected));
        LOGGER.info("Module descriptor counts actual={}", EndpointGrantManagerStartupExpectations.countDescriptorsByModule(actual));

        assertThat(actualKeys).isEqualTo(expectedKeys);
        assertThat(EndpointGrantManagerStartupExpectations.totalDescriptorCount(actual))
                .isEqualTo(EndpointGrantManagerStartupExpectations.totalDescriptorCount(expected));
    }

    @Test
    void platformContractImplementationLoadsDictionaryGrants() {
        Map<String, EndpointModuleGrantCatalogVo> catalogs = endpointGrantManager.allModuleCatalogs();
        EndpointModuleGrantCatalogVo dictionary = catalogs.get("dictionary");
        assertThat(dictionary).isNotNull();

        Set<String> names = EndpointGrantManagerStartupExpectations.allGrantNames(
                Map.of("dictionary", dictionary));
        LOGGER.info("dictionary grant names: {}", names);

        assertThat(names).contains(
                "dictionary.type.page",
                "dictionary.type.get",
                "dictionary.type.create",
                "dictionary.type.delete",
                "dictionary.type.options");

        EndpointGrantDescriptorVo page = EndpointGrantManagerStartupExpectations.streamAll(catalogs)
                .filter(item -> "dictionary.type.page".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(page.resourceClass()).isEqualTo(PlatformDictionaryTypeEndpoint.class.getName());
        assertThat(page.httpMethod()).isEqualTo("GET");
        assertThat(page.path()).isEqualTo(PlatformConstant.DICTIONARY_TYPES_PATH);
    }

    @Test
    void platformStandaloneEndpointLoadsUserModuleGrants() {
        long userDescriptors = EndpointGrantManagerStartupExpectations.descriptorsForResource(PlatformUserEndpoint.class)
                .size();
        assertThat(userDescriptors).isGreaterThanOrEqualTo(1L);

        EndpointGrantDescriptorVo create = EndpointGrantManagerStartupExpectations.streamAll(
                        endpointGrantManager.allModuleCatalogs())
                .filter(item -> "platform.user.create".equals(item.name()))
                .findFirst()
                .orElseThrow();
        LOGGER.info("Platform user grant sample: {}", create);

        assertThat(create.resourceClass()).isEqualTo(PlatformUserEndpoint.class.getName());
        assertThat(endpointGrantManager.match(create.httpMethod(), create.path()))
                .map(EndpointGrantDescriptorVo::name)
                .hasValue("platform.user.create");
    }

    @Test
    void consoleModuleEndpointsLoadCatalogAndPluginGrants() {
        EndpointGrantDescriptorVo catalogTree = EndpointGrantManagerStartupExpectations.streamAll(
                        endpointGrantManager.allModuleCatalogs())
                .filter(item -> "catalog.tree".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(catalogTree.resourceClass()).isEqualTo(ConsoleCatalogEndpoint.class.getName());
        assertThat(catalogTree.path()).isEqualTo(ConsoleConstant.apiPath("/catalog") + "/tree");

        EndpointGrantDescriptorVo pluginList = EndpointGrantManagerStartupExpectations.streamAll(
                        endpointGrantManager.allModuleCatalogs())
                .filter(item -> "plugin.list".equals(item.name()))
                .findFirst()
                .orElseThrow();
        assertThat(pluginList.resourceClass()).isEqualTo(PluginManagementEndpoint.class.getName());

        LOGGER.info("Console catalog grant: {}", catalogTree);
        LOGGER.info("Console plugin grant: {}", pluginList);

        assertThat(endpointGrantManager.match(catalogTree.httpMethod(), catalogTree.path()))
                .map(EndpointGrantDescriptorVo::name)
                .hasValue("catalog.tree");
    }

    @Test
    void matchResolvesPlatformAndConsolePathsAfterStartup() {
        assertThat(endpointGrantManager.match(
                "GET",
                PlatformConstant.DICTIONARY_TYPES_PATH))
                .map(EndpointGrantDescriptorVo::name)
                .hasValue("dictionary.type.page");

        assertThat(endpointGrantManager.match(
                "GET",
                PlatformConstant.DICTIONARY_TYPES_PATH + "/type-1"))
                .map(EndpointGrantDescriptorVo::name)
                .hasValue("dictionary.type.get");

        assertThat(endpointGrantManager.match(
                "GET",
                ConsoleConstant.apiPath("/catalog/tree")))
                .map(EndpointGrantDescriptorVo::name)
                .hasValue("catalog.tree");
    }
}
