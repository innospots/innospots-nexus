package com.innospots.nexus.base.ui.spec.validation;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.PageMeta;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.config.PageDslConfig;
import com.innospots.nexus.base.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.base.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.base.ui.spec.datasource.StaticDataSource;
import com.innospots.nexus.base.ui.spec.HttpRequest;
import com.innospots.nexus.base.ui.spec.node.ComponentNode;
import com.innospots.nexus.base.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.base.ui.spec.parser.JacksonPageDslParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageDslValidatorTest {

    private PageDslValidator validator;
    private JacksonPageDslParser parser;

    @BeforeEach
    void setUp() {
        validator = new PageDslValidator();
        parser = new JacksonPageDslParser(PageDslConfig.defaults());
    }

    @Test
    void acceptsMinimalValidDocument() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));

        assertThatCode(() -> validator.validate(document)).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingDslVersion() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.setDsl("0.9");

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("dsl version");
    }

    @Test
    void rejectsMissingPageId() {
        PageDsl document = PageDsl.of(new PageMeta());

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("page.id");
    }

    @Test
    void rejectsStaticDataSourceWithoutValue() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getDataSources().put("statusOptions", new StaticDataSource());

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("statusOptions");
    }

    @Test
    void rejectsServiceDataSourceWithoutServiceName() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getDataSources().put("customers", new ServiceDataSource());

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("customers");
    }

    @Test
    void rejectsHttpDataSourceWithoutRequestUrl() {
        HttpDataSource dataSource = new HttpDataSource();
        dataSource.setRequest(new HttpRequest());

        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getDataSources().put("customerDetail", dataSource);

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("customerDetail");
    }

    @Test
    void rejectsUnknownComponentReferenceInBody() {
        ComponentReferenceNode body = new ComponentReferenceNode();
        body.setComponent("missingForm");

        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.setBody(body);

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("missingForm");
    }

    @Test
    void rejectsActionReferencingUnknownDataSource() {
        ActionConfig reload = new ActionConfig();
        reload.setAction("reload");
        reload.setParams(Map.of("dataSource", "missing"));

        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getActions().put("search", new ActionOrList(List.of(reload)));

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void rejectsDuplicateActionResultIdsInYaml() {
        assertThatThrownBy(() -> parser.parse("""
                dsl: '1.0'
                page:
                  id: customer-list
                actions:
                  save:
                    - id: saveResult
                      action: request
                      params:
                        method: POST
                        url: /api/save
                    - id: saveResult
                      action: message
                      params:
                        text: done
                """))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("saveResult");
    }

    @Test
    void rejectsComponentNodeWithoutType() {
        PageDsl document = PageDsl.of(PageMeta.of("customer-list"));
        document.getComponents().put("broken", new ComponentNode());

        assertThatThrownBy(() -> validator.validate(document))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("type");
    }
}
