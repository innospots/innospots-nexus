package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageMeta;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.endpoint.DefaultPageDslEndpoint;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.endpoint.PageDslEndpoint;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.PageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageDslFilterContractsTest {

    @Test
    void filterChainRunsInDeclarationOrder() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list", "客户管理"));
        PageDslRenderContext context = PageDslRenderContext.of("sales", "customer-list", document, Map.of());

        PageDsl result = PageDslFilterChain.of(
                contextStep -> {
                    contextStep.attribute("first", true);
                    return contextStep.document();
                },
                contextStep -> {
                    contextStep.attribute("second", true);
                    return contextStep.document();
                }
        ).process(context);

        assertThat(result.getPage().getId()).isEqualTo("customer-list");
        assertThat(context.attributes()).containsEntry("first", true);
        assertThat(context.attributes()).containsEntry("second", true);
    }

    @Test
    void stateBindingFilterAppliesRequestParameters() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        PageDslRenderContext context = PageDslRenderContext.of(
                "sales",
                "customer-list",
                document,
                Map.of("keyword", "Tom", "page", 2));

        PageDsl result = PageDslFilterChain.create()
                .add(new StateBindingPageDslFilter())
                .process(context);

        assertThat(result.state()).containsEntry("keyword", "Tom");
        assertThat(result.state()).containsEntry("page", 2);
    }

    @Test
    void componentFilterCanRemoveElementsFromDocument() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getComponents().put("searchForm", new ComponentNode());
        document.getComponents().put("banner", new ComponentNode());

        PageDsl result = PageDslFilterChain.create()
                .add(contextStep -> {
                    PageDsl current = contextStep.document();
                    current.getComponents().remove("banner");
                    return current;
                })
                .process(PageDslRenderContext.of("sales", "customer-list", document, Map.of()));

        assertThat(result.components()).containsOnlyKeys("searchForm");
    }

    @Test
    void defaultEndpointLoadsAndProcessesThroughFilterChain() {
        PageDsl source = PageDsl.of(PageMeta.of("customer-list"));
        PageDslLoader loader = (moduleKey, pageKey) -> source;
        PageDslEndpoint endpoint = new DefaultPageDslEndpoint(
                loader,
                PageDslFilterChain.create().add(new StateBindingPageDslFilter()));

        PageDsl result = endpoint.render("sales", "customer-list", Map.of("tenantId", "t1"));

        assertThat(result.state()).containsEntry("tenantId", "t1");
    }

    @Test
    void rejectsNullFilterResultsAndMissingContext() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));

        assertThatThrownBy(() -> PageDslFilterChain.create()
                .add(contextStep -> null)
                .process(PageDslRenderContext.of("sales", "customer-list", document, Map.of())))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("null");

        assertThatThrownBy(() -> PageDslFilterChain.create().process(null))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void endpointRejectsMissingPageIdentity() {
        PageDslLoader loader = (moduleKey, pageKey) -> PageDsl.of(PageMeta.of(pageKey));
        PageDslEndpoint endpoint = new DefaultPageDslEndpoint(loader, PageDslFilterChain.create());

        assertThatThrownBy(() -> endpoint.render("", "customer-list", Map.of()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> endpoint.render("sales", "", Map.of()))
                .isInstanceOf(NexusException.class);
    }
}
