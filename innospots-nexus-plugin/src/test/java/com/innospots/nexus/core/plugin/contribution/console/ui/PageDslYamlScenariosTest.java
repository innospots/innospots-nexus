package com.innospots.nexus.core.plugin.contribution.console.ui;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.StaticDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.endpoint.DefaultPageDslEndpoint;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter.PageDslFilterChain;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter.StateBindingPageDslFilter;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslServiceSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end verification of the canonical {@code customer-list.yaml} fixture.
 *
 * <p>Covers parser, validator, loader, filter chain, and endpoint integration for a
 * representative full Pactor page document.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PageDslYamlScenariosTest {

    private static final String FIXTURE = "ui-pages/demo/customer-list.yaml";

    private PageDsl document;
    private String yamlContent;

    @BeforeAll
    void loadFixture() throws IOException {
        JacksonPageDslParser parser = new JacksonPageDslParser(PageDslConfig.defaults());
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(FIXTURE)) {
            assertThat(inputStream).as("fixture %s", FIXTURE).isNotNull();
            yamlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        document = parser.parse(yamlContent);
    }

    @Test
    void parsesFullCustomerListFixture() {
        assertThat(document.getDsl()).isEqualTo(PageDsl.SPEC_VERSION);
        assertThat(document.getPage().getId()).isEqualTo("customer-list");
        assertThat(document.getPage().getName()).isEqualTo("customerList");
        assertThat(document.getPage().getType()).isEqualTo("list");
        assertThat(document.getPage().getPermission().getCode()).isEqualTo("customer:view");
        assertThat(document.getRequires().getRuntime()).isEqualTo(">=0.1.0");
        assertThat(document.getRequires().getComponents()).containsKeys("Page", "Form", "Table");
    }

    @Test
    void parsesStateMetaAndDataSourcesFromFixture() {
        assertThat(document.getMeta()).containsEntry("version", "1.0.0");
        assertThat(document.state()).containsEntry("page", 1);
        assertThat(document.state()).containsEntry("pageSize", 20);
        assertThat(document.dataSources().get("statusOptions")).isInstanceOf(StaticDataSource.class);
        assertThat(document.dataSources().get("customers")).isInstanceOf(ServiceDataSource.class);
        assertThat(document.dataSources().get("customerDetail")).isInstanceOf(HttpDataSource.class);
        assertThat(((ServiceDataSource) document.dataSources().get("customers")).getAutoLoad())
                .isTrue();
    }

    @Test
    void parsesNamedActionsComponentsAndLifecycleFromFixture() {
        assertThat(document.actions()).containsKeys("search", "resetSearch", "deleteRow");
        assertThat(document.actions().get("resetSearch").actions()).hasSize(2);
        ActionConfig confirmDelete = document.actions().get("deleteRow").actions().getFirst();
        assertThat(confirmDelete.getId()).isEqualTo("confirmDelete");
        assertThat(confirmDelete.getAction()).isEqualTo("confirm");
        assertThat(confirmDelete.getPermission().getCode()).isEqualTo("customer:delete");

        assertThat(document.components()).containsKeys("searchForm", "rowActions", "extensionPanel");
        DslSourceRef extensionPanel = (DslSourceRef) document.components().get("extensionPanel");
        assertThat(((DslServiceSource) extensionPanel.getSource()).getService())
                .isEqualTo("customer.extensionPanel");
        assertThat(document.getLifecycle().getOnInit().actions()).hasSize(1);
    }

    @Test
    void parsesBodyTreeWithComponentReferencesAndConditionalNodes() {
        ComponentNode page = (ComponentNode) document.getBody();
        assertThat(page.getType()).isEqualTo("Page");
        assertThat(page.getChildren().items()).hasSize(4);

        ComponentReferenceNode searchForm = (ComponentReferenceNode) page.getChildren().items().getFirst();
        ComponentNode alert = (ComponentNode) page.getChildren().items().get(1);
        ComponentNode table = (ComponentNode) page.getChildren().items().get(2);
        ComponentReferenceNode extensionPanel = (ComponentReferenceNode) page.getChildren().items().get(3);

        assertThat(searchForm.getComponent()).isEqualTo("searchForm");
        assertThat(alert.getWhen()).isEqualTo("${dataStatus.customers.error != null}");
        assertThat(table.getType()).isEqualTo("Table");
        assertThat(extensionPanel.getComponent()).isEqualTo("extensionPanel");
    }

    @Test
    void loadsFixtureThroughClasspathLoaderAndEndpointPipeline() {
        PageDslConfig config = PageDslConfig.defaults();
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                config,
                new JacksonPageDslParser(config),
                getClass().getClassLoader());
        DefaultPageDslEndpoint endpoint = new DefaultPageDslEndpoint(
                loader,
                PageDslFilterChain.create().add(new StateBindingPageDslFilter()));

        PageDsl rendered = endpoint.render(
                "demo",
                "customer-list",
                Map.of("keyword", "Acme", "page", 3));

        assertThat(rendered.getPage().getId()).isEqualTo("customer-list");
        assertThat(rendered.state()).containsEntry("keyword", "Acme");
        assertThat(rendered.state()).containsEntry("page", 3);
        assertThat(rendered.components().get("searchForm")).isInstanceOf(ComponentNode.class);
    }

    @Test
    void printsFixtureVerificationSummary() {
        int actionCount = document.actions().values().stream()
                .mapToInt(action -> action.actions().size())
                .sum();

        String summary = String.format(
                "Pactor DSL fixture verified: page=%s, dataSources=%d, actions=%d, components=%d, bodyChildren=%d, yamlBytes=%d",
                document.getPage().getId(),
                document.dataSources().size(),
                actionCount,
                document.components().size(),
                ((ComponentNode) document.getBody()).getChildren().items().size(),
                yamlContent.getBytes(StandardCharsets.UTF_8).length);

        System.out.println(summary);
        assertThat(summary).contains("customer-list");
    }
}
