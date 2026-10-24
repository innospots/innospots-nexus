package com.innospots.nexus.console.logger;

import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;

public final class EndpointGrantFixtures {

    private EndpointGrantFixtures() {
    }

    public static ResolvedEndpointGrant dictionaryTypeCreateGrant() {
        return new ResolvedEndpointGrant(
                "dictionary",
                "platform",
                ResolvedEndpointGrant.DEFAULT_GRANT_GROUP,
                EndpointGrantAction.CREATE,
                "dictionary.type.create",
                dictionaryTypeCreateLabel(),
                true,
                true,
                false,
                false);
    }

    private static I18nLiteral dictionaryTypeCreateLabel() {
        return new I18nLiteral() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return I18nLiteral.class;
            }

            @Override
            public String[] pairs() {
                return new String[] {"zh-CN", "创建字典类型", "en", "Create dictionary type"};
            }
        };
    }
}
