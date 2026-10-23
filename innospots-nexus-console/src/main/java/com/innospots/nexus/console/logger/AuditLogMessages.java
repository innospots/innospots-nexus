package com.innospots.nexus.console.logger;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.endpoint.grant.I18nLiteralSupport;

/**
 * 将 {@link I18nLiteral} 转为 {@link I18nObject}，并按当前线程语言环境解析审计文案。
 */
public final class AuditLogMessages {

    private AuditLogMessages() {
    }

    /**
     * 将嵌套 {@link I18nLiteral} 转为 {@link I18nObject}。
     *
     * @param literal 注解字面量
     * @return 多语言对象；无 pairs 时返回空对象
     */
    public static I18nObject toI18nObject(I18nLiteral literal) {
        return I18nLiteralSupport.toI18nObject(literal);
    }

    /**
     * 按 {@link com.innospots.nexus.base.i18n.I18nConverter#locale()} 解析操作说明。
     *
     * @param literal {@link com.innospots.nexus.core.endpoint.grant.EndpointGrant#label()}
     * @return 当前语言下的描述；无文案时为空字符串
     */
    public static String resolveOperationMessage(I18nLiteral literal) {
        return I18nLiteralSupport.resolveOperationMessage(literal);
    }
}
