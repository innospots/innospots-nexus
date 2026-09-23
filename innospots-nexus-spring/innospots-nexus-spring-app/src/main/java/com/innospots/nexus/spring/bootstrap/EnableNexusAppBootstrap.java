package com.innospots.nexus.spring.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.spring.core.bootstrap.EnableNexusHostBootstrap;

/**
 * 显式启用 API 应用服务启动引导装配。
 *
 * <p>组合 {@link EnableNexusHostBootstrap}；插件运行时另需
 * {@link com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusHostBootstrap
public @interface EnableNexusAppBootstrap {
}
