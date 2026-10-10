package com.innospots.nexus.spring.core.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.core.i18n.EnableNexusI18n;
import com.innospots.nexus.spring.core.jaxrs.EnableNexusJaxRs;

/**
 * 轻量 Nexus 宿主引导：持久化公共运行态、声明式事务、i18n、通用 JAX-RS 与启动编排，
 * 不注册 Nexus 平台表 DAO（如系统设置表）。
 *
 * <p>适用于依赖 Nexus 公共 Web/持久化基础、自行按需 {@code @Import} 各域
 * {@code *DaoConfiguration} 的外部模块。需要 {@code nx_system_setting} 时请使用
 * {@link EnableNexusHostBootstrap}。</p>
 *
 * @see EnableNexusHostBootstrap
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusI18n
@EnableNexusJaxRs
@Import({
        NexusPersistenceConfiguration.class,
        NexusTransactionConfiguration.class,
        NexusStartupConfiguration.class
})
public @interface EnableNexusSimpleBootstrap {
}
