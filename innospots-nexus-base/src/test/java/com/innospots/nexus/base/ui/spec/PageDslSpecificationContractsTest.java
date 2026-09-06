package com.innospots.nexus.base.ui.spec;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.config.PageDslConfig;
import com.innospots.nexus.base.ui.spec.datasource.ComputedDataSource;
import com.innospots.nexus.base.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.base.ui.spec.datasource.ResourceDataSource;
import com.innospots.nexus.base.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.base.ui.spec.datasource.StaticDataSource;
import com.innospots.nexus.base.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.base.ui.spec.node.ComponentNode;
import com.innospots.nexus.base.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.base.ui.spec.node.DslSourceRef;
import com.innospots.nexus.base.ui.spec.node.DslServiceSource;
import com.innospots.nexus.base.ui.spec.permission.PermissionConfig;
import com.innospots.nexus.base.ui.spec.permission.PermissionDenied;
import com.innospots.nexus.base.ui.spec.parser.JacksonPageDslParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies key Pactor DSL 1.0 modules and component bindings through YAML scenarios.
 */
class PageDslSpecificationContractsTest {

    private JacksonPageDslParser parser;

    @BeforeEach
    void setUp() {
        parser = new JacksonPageDslParser(PageDslConfig.defaults());
    }

    @Test
    void parsesAllDataSourceTypes() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: datasource-demo
                dataSources:
                  statusOptions:
                    type: static
                    value:
                      - label: 正常
                        value: active
                  customers:
                    type: service
                    service: customer.list
                  customerDetail:
                    type: http
                    request:
                      method: GET
                      url: /api/customers/1
                  filteredCustomers:
                    type: computed
                    expression: ${data.customers}
                    dependsOn:
                      - customers
                  roleOptions:
                    type: resource
                    resource: roles
                """);

        assertThat(document.dataSources().get("statusOptions")).isInstanceOf(StaticDataSource.class);
        assertThat(document.dataSources().get("customers")).isInstanceOf(ServiceDataSource.class);
        assertThat(document.dataSources().get("customerDetail")).isInstanceOf(HttpDataSource.class);
        assertThat(document.dataSources().get("filteredCustomers")).isInstanceOf(ComputedDataSource.class);
        assertThat(document.dataSources().get("roleOptions")).isInstanceOf(ResourceDataSource.class);
        assertThat(((ServiceDataSource) document.dataSources().get("customers")).getService())
                .isEqualTo("customer.list");
    }

    @Test
    void parsesPermissionDeclarationForms() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: permission-demo
                  permission: customer:view
                body:
                  type: Button
                  permission:
                    - customer:edit
                    - customer:admin
                  children:
                    - type: Button
                      permission:
                        code: customer:delete
                        denied: disabled
                """);

        assertThat(document.getPage().getPermission().getKind())
                .isEqualTo(PermissionConfig.PermissionKind.CODE);
        assertThat(document.getPage().getPermission().getCode()).isEqualTo("customer:view");

        ComponentNode body = (ComponentNode) document.getBody();
        assertThat(body.getPermission().getKind()).isEqualTo(PermissionConfig.PermissionKind.ANY_OF);
        assertThat(body.getPermission().getAnyOf()).containsExactly("customer:edit", "customer:admin");

        ComponentNode nested = (ComponentNode) body.getChildren().items().getFirst();
        assertThat(nested.getPermission().getKind()).isEqualTo(PermissionConfig.PermissionKind.DETAILED);
        assertThat(nested.getPermission().getCode()).isEqualTo("customer:delete");
        assertThat(nested.getPermission().getDenied()).isEqualTo(PermissionDenied.DISABLED);
    }

    @Test
    void parsesActionAsSingleObjectOrArray() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: action-demo
                dataSources:
                  customers:
                    type: service
                    service: customer.list
                actions:
                  single:
                    action: reload
                    params:
                      dataSource: customers
                  multiple:
                    - action: resetState
                    - action: reload
                      params:
                        dataSource: customers
                """);

        assertThat(document.actions().get("single").actions()).hasSize(1);
        assertThat(document.actions().get("single").actions().getFirst().getAction()).isEqualTo("reload");
        assertThat(document.actions().get("multiple").actions()).hasSize(2);
        assertThat(document.actions().get("multiple").actions().get(1).getAction()).isEqualTo("reload");
    }

    @Test
    void parsesComponentNodeAndReferenceNode() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: node-demo
                components:
                  searchForm:
                    type: Form
                    props:
                      name: search
                body:
                  type: Page
                  children:
                    - component: searchForm
                    - type: Alert
                      when: ${state.ready}
                      props:
                        message: ready
                """);

        ComponentReferenceNode reference = (ComponentReferenceNode) ((ComponentNode) document.getBody())
                .getChildren().items().getFirst();
        ComponentNode inline = (ComponentNode) ((ComponentNode) document.getBody())
                .getChildren().items().get(1);

        assertThat(reference.getComponent()).isEqualTo("searchForm");
        assertThat(inline.getType()).isEqualTo("Alert");
        assertThat(inline.getWhen()).isEqualTo("${state.ready}");
    }

    @Test
    void parsesDynamicDslSourceRefInComponents() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: dynamic-demo
                components:
                  extensionPanel:
                    source:
                      type: service
                      service: customer.extensionPanel
                      params:
                        customerId: ${state.selectedId}
                    placeholder:
                      type: Alert
                      props:
                        message: 加载中
                """);

        DslSourceRef extensionPanel = (DslSourceRef) document.components().get("extensionPanel");
        DslServiceSource source = (DslServiceSource) extensionPanel.getSource();

        assertThat(source.getService()).isEqualTo("customer.extensionPanel");
        assertThat(source.getParams()).containsEntry("customerId", "${state.selectedId}");
        assertThat(((ComponentNode) extensionPanel.getPlaceholder()).getType()).isEqualTo("Alert");
    }

    @Test
    void parsesLifecycleAndRequiresSections() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                requires:
                  runtime: '>=0.1.0'
                  components:
                    Table: '>=1.0'
                page:
                  id: lifecycle-demo
                lifecycle:
                  onInit:
                    - action: setState
                      params:
                        ready: true
                  onLoad: []
                """);

        assertThat(document.getRequires().getRuntime()).isEqualTo(">=0.1.0");
        assertThat(document.getRequires().getComponents()).containsEntry("Table", ">=1.0");
        assertThat(document.getLifecycle().getOnInit().actions()).hasSize(1);
        assertThat(document.getLifecycle().getOnLoad().actions()).isEmpty();
    }

    @Test
    void parsesEventActionListsOnComponentNodes() {
        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: event-demo
                actions:
                  refresh:
                    - action: message
                      params:
                        text: refreshed
                body:
                  type: Button
                  events:
                    onClick:
                      - action: call
                        params:
                          name: refresh
                """);

        ComponentNode body = (ComponentNode) document.getBody();
        ActionOrList onClick = body.getEvents().get("onClick");

        assertThat(onClick.actions()).hasSize(1);
        assertThat(onClick.actions().getFirst().getAction()).isEqualTo("call");
        assertThat(onClick.actions().getFirst().getParams()).containsEntry("name", "refresh");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "customer-list",
            "fragment-list"
    })
    void loadsClasspathDemoPages(String pageKey) {
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                PageDslConfig.defaults(),
                parser,
                getClass().getClassLoader());

        PageDsl document = loader.load("demo", pageKey);

        assertThat(document.getDsl()).isEqualTo(PageDsl.SPEC_VERSION);
        assertThat(document.getPage().getId()).isEqualTo(pageKey);
    }

    @Test
    void roundTripsYamlWithoutLosingTopLevelSections() {
        String yaml = """
                dsl: '1.0'
                page:
                  id: roundtrip-demo
                  title: Roundtrip
                state:
                  keyword: ''
                dataSources:
                  customers:
                    type: service
                    service: customer.list
                actions:
                  search:
                    action: reload
                    params:
                      dataSource: customers
                components:
                  searchForm:
                    type: Form
                body:
                  component: searchForm
                """;
        PageDsl original = parser.parse(yaml);
        PageDsl restored = parser.parse(parser.write(original));

        assertThat(restored.getPage().getTitle()).isEqualTo("Roundtrip");
        assertThat(restored.dataSources()).containsKey("customers");
        assertThat(restored.actions()).containsKey("search");
        assertThat(restored.components()).containsKey("searchForm");
        assertThat(((ComponentReferenceNode) restored.getBody()).getComponent()).isEqualTo("searchForm");
    }

    @Test
    void rejectsYamlWithBothTypeAndComponentOnSameNode() {
        assertThatThrownBy(() -> parser.parse("""
                dsl: '1.0'
                page:
                  id: invalid-node
                body:
                  type: Button
                  component: searchForm
                """))
                .isInstanceOf(Exception.class);
    }

    @Test
    void rejectsInvalidDslVersionInYaml() {
        assertThatThrownBy(() -> parser.parse("""
                dsl: '2.0'
                page:
                  id: invalid-version
                """))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("dsl version");
    }
}
