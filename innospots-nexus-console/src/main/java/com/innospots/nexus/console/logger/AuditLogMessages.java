package com.innospots.nexus.console.logger;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;

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
        if (literal == null) {
            return new I18nObject();
        }
        String[] pairs = literal.pairs();
        if (pairs.length == 0) {
            return new I18nObject();
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("I18nLiteral.pairs length must be even");
        }
        I18nObject object = new I18nObject();
        for (int index = 0; index < pairs.length; index += 2) {
            String locale = pairs[index];
            String text = pairs[index + 1];
            if (locale != null && text != null && !locale.isBlank()) {
                object.put(locale.trim(), text);
            }
        }
        return object;
    }

    /**
     * 按 {@link I18nConverter#locale()} 解析操作说明。
     *
     * @param literal {@link com.innospots.nexus.console.endpoint.grant.EndpointGrant#label()}
     * @return 当前语言下的描述；无文案时为空字符串
     */
    public static String resolveOperationMessage(I18nLiteral literal) {
        if (literal == null) {
            return "";
        }
        I18nObject object = toI18nObject(literal);
        if (object.isEmpty()) {
            return "";
        }
        String resolved = object.value(I18nConverter.locale());
        return resolved == null ? "" : resolved.trim();
    }
}
