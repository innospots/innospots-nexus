package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.ui.spec.PageInfo;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.VisibleCondition;
import com.innospots.nexus.base.ui.spec.action.ActionType;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.ComponentType;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.endpoint.DefaultUiSpecEndpoint;
import com.innospots.nexus.base.ui.spec.endpoint.UiSpecEndpoint;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UiSpecFilterContractsTest {

    @Test
    void filterChainRunsInDeclarationOrder() {
        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")));
        UiSpecRenderContext context = UiSpecRenderContext.of("sales", "orders", spec, Map.of());

        UiSpec result = UiSpecFilterChain.of(
                contextStep -> {
                    contextStep.attribute("first", true);
                    return contextStep.spec();
                },
                contextStep -> {
                    contextStep.attribute("second", true);
                    return contextStep.spec();
                }
        ).process(context);

        assertThat(result.pageInfo().pageId()).isEqualTo("orders");
        assertThat(context.attributes()).containsEntry("first", true);
        assertThat(context.attributes()).containsEntry("second", true);
    }

    @Test
    void variableBindingFilterAppliesRequestParameters() {
        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")));
        UiSpecRenderContext context = UiSpecRenderContext.of(
                "sales",
                "orders",
                spec,
                Map.of("canView", true, "groupIds", "g1"));

        UiSpec result = UiSpecFilterChain.create()
                .add(new VariableBindingUiSpecFilter())
                .process(context);

        assertThat(result.variables().get("canView").defaultValue()).isEqualTo(true);
        assertThat(result.variables().get("groupIds").defaultValue()).isEqualTo("g1");
    }

    @Test
    void actionVisibilityFilterRemovesHiddenActions() {
        UiSpec spec = UiSpec.create()
                .actionDefinition(UiAction.of("delete", ActionType.API)
                        .visibleIf(VisibleCondition.expression("${canDelete}")))
                .actionDefinition(UiAction.of("view", ActionType.LINK)
                        .visibleIf(VisibleCondition.expression("${canView}")));

        UiSpecRenderContext context = UiSpecRenderContext.of(
                "sales",
                "orders",
                spec,
                Map.of("canDelete", false, "canView", true));

        UiSpec result = UiSpecFilterChain.of(
                new VariableBindingUiSpecFilter(),
                new ActionVisibilityUiSpecFilter(
                        (expression, variables) -> Boolean.TRUE.equals(variables.get(expression)))
        ).process(context);

        assertThat(result.actionDefinitions()).containsOnlyKeys("view");
    }

    @Test
    void componentFilterCanRemoveElementsFromSpec() {
        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")))
                .component(UiComponentSpec.of("table", ComponentType.TABLE))
                .component(UiComponentSpec.of("banner", ComponentType.BANNER_CARD));

        UiSpec result = UiSpecFilterChain.create()
                .add(contextStep -> {
                    UiSpec current = contextStep.spec();
                    current.getComponents().remove("banner");
                    return current;
                })
                .process(UiSpecRenderContext.of("sales", "orders", spec, Map.of()));

        assertThat(result.components()).containsOnlyKeys("table");
    }

    @Test
    void defaultEndpointLoadsAndProcessesThroughFilterChain() {
        UiSpec source = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")));
        UiSpecLoader loader = (moduleKey, pageKey) -> source;
        UiSpecEndpoint endpoint = new DefaultUiSpecEndpoint(
                loader,
                UiSpecFilterChain.create().add(new VariableBindingUiSpecFilter()));

        UiSpec result = endpoint.render("sales", "orders", Map.of("tenantId", "t1"));

        assertThat(result.variables().get("tenantId").defaultValue()).isEqualTo("t1");
    }

    @Test
    void rejectsNullFilterResultsAndMissingContext() {
        UiSpec spec = UiSpec.page(PageInfo.of("orders", I18nObject.of("Orders")));

        assertThatThrownBy(() -> UiSpecFilterChain.create()
                .add(contextStep -> null)
                .process(UiSpecRenderContext.of("sales", "orders", spec, Map.of())))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("null");

        assertThatThrownBy(() -> UiSpecFilterChain.create().process(null))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void endpointRejectsMissingPageIdentity() {
        UiSpecLoader loader = (moduleKey, pageKey) -> UiSpec.page(PageInfo.of(pageKey, I18nObject.of("T")));
        UiSpecEndpoint endpoint = new DefaultUiSpecEndpoint(loader, UiSpecFilterChain.create());

        assertThatThrownBy(() -> endpoint.render("", "orders", Map.of()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> endpoint.render("sales", "", Map.of()))
                .isInstanceOf(NexusException.class);
    }
}
