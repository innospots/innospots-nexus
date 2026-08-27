package com.innospots.nexus.base.ui;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.ui.spec.ApiRequest;
import com.innospots.nexus.base.ui.spec.PageInfo;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.VisibleCondition;
import com.innospots.nexus.base.ui.spec.action.ActionType;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.ComponentType;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.config.UiSpecConfig;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;
import com.innospots.nexus.base.ui.spec.form.FormField;
import com.innospots.nexus.base.ui.spec.form.OptionItem;
import com.innospots.nexus.base.ui.spec.layout.ComponentRef;
import com.innospots.nexus.base.ui.spec.layout.LayoutType;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import com.innospots.nexus.base.ui.spec.loader.ClasspathUiSpecLoader;
import com.innospots.nexus.base.ui.spec.parser.JacksonUiSpecParser;
import com.innospots.nexus.base.ui.spec.table.TableColumn;
import com.innospots.nexus.base.ui.spec.table.UiTable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UiSpecContractsTest {

    @Test
    void buildsPageSpecWithComponentActionAndLayoutContracts() {
        UiAction refresh = UiAction.of("refresh", ActionType.REFRESH)
                .label(I18nObject.of("en", "Refresh", "zh", "刷新"))
                .icon("refresh")
                .visibleIf(VisibleCondition.expression("${enabled}"));
        UiComponentSpec table = UiComponentSpec.of("orders", ComponentType.TABLE)
                .title(I18nObject.of("Orders"))
                .datasource("orders")
                .table(UiTable.create()
                        .pagination(true)
                        .pageSize(20)
                        .column(TableColumn.named("Order No", "orderNo", "String")))
                .action(refresh);
        UiLayout layout = UiLayout.of("main", LayoutType.GRID)
                .gap(12)
                .component(ComponentRef.of("orders").span(12));

        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")))
                .component(table)
                .layout(layout)
                .actionDefinition(refresh.datasourceKey("orders"))
                .datasource("orders", UiDatasource.get("/api/orders")
                        .param("groupIds", "${groupIds}"));

        assertThat(spec.components()).containsKey("orders");
        assertThat(spec.actionDefinitions()).containsKey("refresh");
        assertThat(spec.datasources().get("orders").params())
                .containsEntry("groupIds", "${groupIds}");
        assertThat(spec.layout().components()).singleElement().extracting(ComponentRef::componentId).isEqualTo("orders");
    }

    @Test
    void uiSpecAddsVariablesAndFiltersActionDefinitions() {
        UiSpec spec = UiSpec.create()
                .actionDefinition(UiAction.of("delete", ActionType.API)
                        .request(ApiRequest.delete("/api/orders/${id}"))
                        .visibleIf(VisibleCondition.expression("${canDelete}")))
                .actionDefinition(UiAction.of("view", ActionType.LINK)
                        .visibleIf(VisibleCondition.expression("${canView}")));

        spec.addVariableValues(Map.of("canDelete", false, "canView", true));
        spec.filterActionDefinitions((expression, context) -> Boolean.TRUE.equals(context.get(expression)));

        assertThat(spec.variables()).containsKeys("canDelete", "canView");
        assertThat(spec.actionDefinitions()).containsOnlyKeys("view");
    }

    @Test
    void keepsFormOptionsAndSerializesJson() {
        UiComponentSpec form = UiComponentSpec.of("profile", ComponentType.FORM)
                .formField(FormField.named("status", "status", "select")
                        .label(I18nObject.of("Status"))
                        .option(OptionItem.of("enabled", I18nObject.of("Enabled"))));

        String json = Jsons.toJson(form);
        UiComponentSpec restored = Jsons.fromJson(json, UiComponentSpec.class);

        assertThat(restored.componentId()).isEqualTo("profile");
        assertThat(restored.formFields()).hasSize(1);
        assertThat(restored.formFields().getFirst().options()).hasSize(1);
    }

    @Test
    void parsesUiSpecFromYamlWithJackson() {
        String yaml = """
                pageInfo:
                  pageId: orders
                  title:
                    en: Orders
                pageType: dashboard
                variables:
                  canView:
                    name: canView
                    type: Boolean
                    defaultValue: true
                datasources:
                  orders:
                    method: GET
                    url: /api/orders
                actionDefinitions:
                  view:
                    actionId: view
                    actionType: link
                    datasourceKey: orders
                    visibleIf:
                      expression: ${canView}
                """;

        JacksonUiSpecParser parser = new JacksonUiSpecParser(UiSpecConfig.defaults());
        UiSpec spec = parser.parse(yaml);

        assertThat(spec.pageType()).isEqualTo("dashboard");
        assertThat(spec.pageInfo().pageId()).isEqualTo("orders");
        assertThat(spec.datasources()).containsKey("orders");
        assertThat(spec.actionDefinitions()).containsKey("view");
    }

    @Test
    void rejectsUnknownUiSpecFieldsAndDatasourceReferences() {
        JacksonUiSpecParser parser = new JacksonUiSpecParser(UiSpecConfig.defaults());

        assertThatThrownBy(() -> parser.parse("""
                pageInfo:
                  pageId: orders
                unsupported: true
                """))
                .isInstanceOf(NexusException.class);

        assertThatThrownBy(() -> parser.parse("""
                pageInfo:
                  pageId: orders
                actionDefinitions:
                  export:
                    actionId: export
                    actionType: api
                    datasourceKey: missing
                """))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void loadsUiSpecFromConfiguredClasspathLocation() {
        UiSpecConfig config = UiSpecConfig.defaults();
        ClasspathUiSpecLoader loader = new ClasspathUiSpecLoader(
                config,
                new JacksonUiSpecParser(config),
                getClass().getClassLoader());

        UiSpec spec = loader.load("sales", "order-list");

        assertThat(spec.pageInfo().pageId()).isEqualTo("order-list");
        assertThat(spec.datasources()).containsKey("orders");
        assertThat(config.resourcePath("sales", "order-list"))
                .isEqualTo("ui-spec/sales/order-list.yaml");
    }

    @Test
    void writesYamlAndSupportsConfiguredYamlSuffix() {
        UiSpecConfig config = new UiSpecConfig(
                "pages",
                ".yml",
                true);
        JacksonUiSpecParser parser = new JacksonUiSpecParser(config);

        UiSpec spec = parser.parse("""
                pageInfo:
                  pageId: orders
                pageType: table
                datasources:
                  orders:
                    method: GET
                    url: /api/orders
                """);
        String yaml = parser.write(spec);

        assertThat(spec.pageInfo().pageId()).isEqualTo("orders");
        assertThat(parser.parse(yaml).pageType()).isEqualTo("table");
        assertThat(config.resourcePath("sales", "orders"))
                .isEqualTo("pages/sales/orders.yml");
    }

    @Test
    void rejectsNonYamlUiSpecSuffix() {
        assertThatThrownBy(() -> new UiSpecConfig("pages", ".json", true))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining(".yaml");
    }

    @Test
    void rejectsUnsafeUiSpecResourceKeys() {
        UiSpecConfig config = UiSpecConfig.defaults();

        assertThatThrownBy(() -> config.resourcePath("../sales", "orders"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("moduleKey");
        assertThatThrownBy(() -> config.resourcePath("sales", "../orders"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("pageKey");
    }
}
