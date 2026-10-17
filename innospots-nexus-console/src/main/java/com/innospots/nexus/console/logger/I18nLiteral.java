package com.innospots.nexus.console.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解内嵌套的多语言文案声明。
 * <p>Java 注解成员不能直接使用 {@link com.innospots.nexus.base.i18n.I18nObject}，
 * 通过 {@code pairs} 交替传入 BCP 47 语言标签与文案，运行时由
 * {@link AuditLogMessages} 转换为 {@link com.innospots.nexus.base.i18n.I18nObject}。</p>
 *
 * @author Smars
 * @date 2026/10/17
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
