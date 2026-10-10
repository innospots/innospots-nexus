package com.innospots.nexus.spring.core.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.core.setting.NexusSystemSettingConfiguration;

/**
 * 完整 Nexus 宿主公共启动引导：在 {@link EnableNexusSimpleBootstrap} 之上叠加系统设置表
 *（{@code nx_system_setting}）装配。
 *
 * <p>由 API 应用与管理控制台 runnable 通过各自 {@code @Enable*} 组合注解复用。
 * 插件运行时须额外标注 {@link com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost}。</p>
 *
 * @see EnableNexusSimpleBootstrap
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusSimpleBootstrap
@Import(NexusSystemSettingConfiguration.class)
public @interface EnableNexusHostBootstrap {
}
