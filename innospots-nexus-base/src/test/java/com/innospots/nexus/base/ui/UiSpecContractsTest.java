package com.innospots.nexus.base.ui;

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
import com.innospots.nexus.base.ui.spec.dsl.UiSpecDsl;
import com.innospots.nexus.base.ui.spec.dsl.UiSpecDslLoader;
import com.innospots.nexus.base.ui.spec.form.FormField;
import com.innospots.nexus.base.ui.spec.form.OptionItem;
import com.innospots.nexus.base.ui.spec.layout.ComponentRef;
import com.innospots.nexus.base.ui.spec.layout.LayoutType;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import com.innospots.nexus.base.ui.spec.table.TableColumn;
import com.innospots.nexus.base.ui.spec.table.UiTable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
                .actionDefinition(refresh)
                .datasource("orders", ApiRequest.get("/api/orders"));

        assertThat(spec.components()).containsKey("orders");
        assertThat(spec.actionDefinitions()).containsKey("refresh");
        assertThat(spec.layout().components()).singleElement().extracting(ComponentRef::componentId).isEqualTo("orders");
    }

    @Test
    void dslAddsVariablesAndFiltersActionDefinitions() {
        UiSpecDsl dsl = UiSpecDsl.create()
                .actionDefinition(UiAction.of("delete", ActionType.API)
                        .request(ApiRequest.delete("/api/orders/${id}"))
                        .visibleIf(VisibleCondition.expression("${canDelete}")))
                .actionDefinition(UiAction.of("view", ActionType.LINK)
                        .visibleIf(VisibleCondition.expression("${canView}")));

        dsl.addVariableValues(Map.of("canDelete", false, "canView", true));
        dsl.filterActionDefinitions((expression, context) -> Boolean.TRUE.equals(context.get(expression)));

        assertThat(dsl.variables()).containsKeys("canDelete", "canView");
        assertThat(dsl.actionDefinitions()).containsOnlyKeys("view");
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
    void keepsLegacyUiPageAndResourceShape() {
        UiPage page = UiPage.of("home", "Home", "/home")
                .component(UiComponent.named("name", "input")
                        .valueType("String")
                        .setting("placeholder", "Name"));
        UiResource resource = UiResource.create()
                .page(page)
                .schema("orders", Map.of("uri", "/api/orders"));

        assertThat(resource.pages()).singleElement().extracting(UiPage::pageKey).isEqualTo("home");
        assertThat(page.components()).containsKey("name");
        assertThat(resource.schemas()).containsKey("orders");
    }

    @Test
    void loadsDslFromYamlWithJackson() {
        String yaml = """
                pageInfo:
                  pageId: orders
                  title:
                    en: Orders
                type: dashboard
                variables:
                  canView:
                    name: canView
                    type: Boolean
                    defaultValue: true
                datasources:
                  orders:
                    method: GET
                    uri: /api/orders
                actionDefinitions:
                  view:
                    actionId: view
                    actionType: link
                    visibleIf:
                      expression: ${canView}
                """;

        UiSpecDsl dsl = UiSpecDslLoader.fromYaml(yaml);

        assertThat(dsl.type()).isEqualTo("dashboard");
        assertThat(dsl.pageInfo().pageId()).isEqualTo("orders");
        assertThat(dsl.datasources()).containsKey("orders");
        assertThat(dsl.actionDefinitions()).containsKey("view");
    }
}
