package com.innospots.nexus.spring.core.i18n;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 显式启用 Nexus i18n Spring 装配（MessageSource 桥接与可选 Web locale 同步）。
 *
 * @author Smars
 * @date 2026/09/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        NexusI18nConfiguration.class,
        NexusI18nWebConfiguration.class
})
public @interface EnableNexusI18n {
}
