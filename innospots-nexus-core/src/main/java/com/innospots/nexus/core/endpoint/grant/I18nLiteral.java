package com.innospots.nexus.core.endpoint.grant;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解内嵌套的多语言文案声明。
 * <p>Java 注解成员不能直接使用 {@link com.innospots.nexus.base.i18n.I18nObject}，
 * 通过 {@code pairs} 交替传入 BCP 47 语言标签与文案，运行时由
 * {@link I18nLiteralSupport} 转换为 {@link com.innospots.nexus.base.i18n.I18nObject}
 * （用于 {@link EndpointGrant#label()}）。</p>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface I18nLiteral {

    /**
     * 语言环境与文案对，形如 {@code "zh-CN", "创建用户", "en", "Create user"}。
     */
    String[] pairs() default {};
}
