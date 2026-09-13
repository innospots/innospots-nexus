package com.innospots.nexus.core.plugin.contribution.console.ui;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageMeta;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.JacksonPageDslParser;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.HttpRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageDslContractsTest {

    @Test
    void buildsPageDslWithCoreContracts() {
        HttpDataSource customers = new HttpDataSource();
        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        request.setUrl("/api/customers");
        customers.setRequest(request);

        ActionConfig reload = new ActionConfig();
        reload.setAction("reload");
        reload.setParams(Map.of("dataSource", "customers"));

        ComponentNode searchForm = new ComponentNode();
        searchForm.setType("Form");

        PageDsl document = PageDsl.of(PageMeta.of("customer-list", "客户管理"));
        document.getDataSources().put("customers", customers);
        document.getActions().put("search", new ActionOrList(List.of(reload)));
        document.getComponents().put("searchForm", searchForm);

        ComponentReferenceNode body = new ComponentReferenceNode();
        body.setComponent("searchForm");
        document.setBody(body);

        assertThat(document.dataSources()).containsKey("customers");
        assertThat(document.actions()).containsKey("search");
        assertThat(document.components()).containsKey("searchForm");
        assertThat(((ComponentReferenceNode) document.getBody()).getComponent()).isEqualTo("searchForm");
    }

    @Test
    void bindsRuntimeStateValues() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getState().put("keyword", "");
        document.bindState(Map.of("keyword", "Tom", "page", 2));

        assertThat(document.state()).containsEntry("keyword", "Tom");
        assertThat(document.state()).containsEntry("page", 2);
    }

    @Test
    void parsesPageDslFromYamlWithJackson() {
        String yaml = """
                dsl: '1.0'
                page:
                  id: customer-list
                  title: 客户管理
                  type: list
                state:
                  keyword: ''
                dataSources:
                  customers:
                    type: service
                    service: customer.list
                    autoLoad: true
                actions:
                  search:
                    - action: reload
                      params:
                        dataSource: customers
                """;

        JacksonPageDslParser parser = new JacksonPageDslParser(PageDslConfig.defaults());
        PageDsl document = parser.parse(yaml);

        assertThat(document.getPage().getId()).isEqualTo("customer-list");
        assertThat(document.getPage().getType()).isEqualTo("list");
        assertThat(document.dataSources().get("customers")).isInstanceOf(ServiceDataSource.class);
        assertThat(document.actions()).containsKey("search");
    }

    @Test
    void serializesComponentNodeAsJson() {
        ComponentNode button = new ComponentNode();
        button.setType("Button");
        button.setProps(Map.of("text", "保存"));

        String json = Jsons.toJson(button);
        ComponentNode restored = Jsons.fromJson(json, ComponentNode.class);

        assertThat(restored.getType()).isEqualTo("Button");
        assertThat(restored.getProps()).containsEntry("text", "保存");
    }

    @Test
    void rejectsUnknownPageDslFieldsAndMissingReferences() {
        JacksonPageDslParser parser = new JacksonPageDslParser(PageDslConfig.defaults());

        assertThatThrownBy(() -> parser.parse("""
                dsl: '1.0'
                page:
                  id: customer-list
                unsupported: true
                """))
                .isInstanceOf(NexusException.class);

        assertThatThrownBy(() -> parser.parse("""
                dsl: '1.0'
                page:
                  id: customer-list
                body:
                  component: missingComponent
                """))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("missingComponent");
    }

    @Test
    void loadsPageDslFromConfiguredClasspathLocation() {
        PageDslConfig config = PageDslConfig.defaults();
        ClasspathPageDslLoader loader = new ClasspathPageDslLoader(
                config,
                new JacksonPageDslParser(config),
                getClass().getClassLoader());

        PageDsl document = loader.load("sales", "order-list");

        assertThat(document.getPage().getId()).isEqualTo("order-list");
        assertThat(document.dataSources()).containsKey("orders");
        assertThat(config.resourcePath("sales", "order-list"))
                .isEqualTo("ui-pages/sales/order-list.yaml");
    }

    @Test
    void writesYamlAndSupportsConfiguredYamlSuffix() {
        PageDslConfig config = new PageDslConfig(
                "ui-pages",
                ".yml",
                true);
        JacksonPageDslParser parser = new JacksonPageDslParser(config);

        PageDsl document = parser.parse("""
                dsl: '1.0'
                page:
                  id: order-list
                dataSources:
                  orders:
                    type: http
                    request:
                      method: GET
                      url: /api/orders
                """);
        String yaml = parser.write(document);

        assertThat(document.getPage().getId()).isEqualTo("order-list");
        assertThat(parser.parse(yaml).dataSources()).containsKey("orders");
        assertThat(config.resourcePath("sales", "order-list"))
                .isEqualTo("ui-pages/sales/order-list.yml");
    }

    @Test
    void rejectsNonPageDslSuffix() {
        assertThatThrownBy(() -> new PageDslConfig("ui-pages", ".page.yaml", true))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining(".yaml");
    }

    @Test
    void rejectsUnsafePageDslResourceKeys() {
        PageDslConfig config = PageDslConfig.defaults();

        assertThatThrownBy(() -> config.resourcePath("../sales", "order-list"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("moduleKey");
        assertThatThrownBy(() -> config.resourcePath("sales", "../order-list"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("pageKey");
    }
}
