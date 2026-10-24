package com.innospots.nexus.core.endpoint.grant.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.logger.AuditLogMessages;
import com.innospots.nexus.console.logger.EndpointGrantFixtures;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

class ResolvedEndpointGrantTest {

    @Test
    void compactConstructorNormalizesNullModuleKeyAndName(TestReporter reporter) {
        I18nLiteral emptyLabel = GrantLabelFixtures.emptyLabel();

        ResolvedEndpointGrant grant = new ResolvedEndpointGrant(
                null,
                null,
                null,
                EndpointGrantAction.READ,
                null,
                emptyLabel,
                true,
                false,
                false,
                false);

        reporter.publishEntry("input", "moduleKey=null, name=null, label=empty");
        reporter.publishEntry("output", grant.toString());

        assertThat(grant.moduleKey()).isEmpty();
        assertThat(grant.domainKey()).isEmpty();
        assertThat(grant.grantGroup()).isEqualTo(ResolvedEndpointGrant.DEFAULT_GRANT_GROUP);
        assertThat(grant.name()).isEmpty();
        assertThat(grant.action()).isEqualTo(EndpointGrantAction.READ);
        assertThat(grant.permissionGrantName()).isEqualTo(ResolvedEndpointGrant.DEFAULT_GRANT_GROUP);
        assertThat(AuditLogMessages.toI18nObject(grant.label()).isEmpty()).isTrue();
        assertThat(grant.recordArgs()).isTrue();
        assertThat(grant.auditLog()).isFalse();
        assertThat(grant.ignorePermission()).isFalse();
        assertThat(grant.ignoreAuth()).isFalse();
    }

    @Test
    void preservesExplicitFields(TestReporter reporter) {
        I18nLiteral label = EndpointGrantFixtures.dictionaryTypeCreateGrant().label();

        ResolvedEndpointGrant grant = new ResolvedEndpointGrant(
                "dictionary",
                "platform",
                "ops",
                EndpointGrantAction.CREATE,
                "dictionary.type.create",
                label,
                false,
                true,
                true,
                true);

        reporter.publishEntry("input", "moduleKey=dictionary, name=dictionary.type.create");
        reporter.publishEntry("output", grant.toString());

        assertThat(grant.moduleKey()).isEqualTo("dictionary");
        assertThat(grant.domainKey()).isEqualTo("platform");
        assertThat(grant.grantGroup()).isEqualTo("ops");
        assertThat(grant.name()).isEqualTo("dictionary.type.create");
        assertThat(grant.permissionGrantName()).isEqualTo("dictionary.type.create");
        assertThat(grant.ignorePermission()).isTrue();
        assertThat(grant.ignoreAuth()).isTrue();
        assertThat(AuditLogMessages.toI18nObject(grant.label()).cnValue()).isEqualTo("创建字典类型");
    }

    /**
     * 通过带 {@link EndpointGrant} 的方法暴露嵌套 {@link I18nLiteral}，供 record 构造测试使用。
     */
    private interface GrantLabelFixtures {

        @EndpointGrant(action = EndpointGrantAction.READ, name = "fixture")
        void empty();

        static I18nLiteral emptyLabel() {
            try {
                return GrantLabelFixtures.class.getMethod("empty")
                        .getAnnotation(EndpointGrant.class)
                        .label();
            } catch (NoSuchMethodException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}
