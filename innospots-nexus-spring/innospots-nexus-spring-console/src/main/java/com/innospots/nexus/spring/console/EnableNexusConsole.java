package com.innospots.nexus.spring.console;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.spring.plugin.config.EnableNexusPluginHost;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.plugin.config.PluginConsoleConfiguration;
import com.innospots.nexus.spring.plugin.config.PluginContributionConfiguration;
import com.innospots.nexus.spring.plugin.config.PluginHostConfiguration;

/**
 * 显式启用 Nexus 管理控制台 Spring 装配。
 *
 * <p>在控制台应用主配置类上标注，依次引入插件宿主、Contribution 三连与管理 REST。
 * 已包含 {@link PluginHostConfiguration}，无需再单独标注
 * {@link EnableNexusPluginHost}。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        PluginHostConfiguration.class,
        PluginContributionConfiguration.class,
        PluginConsoleConfiguration.class
})
public @interface EnableNexusConsole {
}
