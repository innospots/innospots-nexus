package com.innospots.nexus.spring.console;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleCatalogConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleDictionaryConfiguration;
import com.innospots.nexus.spring.console.config.ConsoleMenuConfiguration;
import com.innospots.nexus.spring.console.config.ConsolePluginConfiguration;
import com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost;

/**
 * 显式启用 Nexus 管理控制台完整 Spring 装配。
 *
 * <p>在 {@link EnableNexusSimpleConsole} 之上叠加插件宿主、目录索引、菜单与字典域。</p>
 *
 * @see EnableNexusSimpleConsole
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableNexusSimpleConsole
@EnableNexusPluginHost
@Import({
        ConsolePluginConfiguration.class,
        ConsoleCatalogConfiguration.class,
        ConsoleMenuConfiguration.class,
        ConsoleDictionaryConfiguration.class
})
public @interface EnableNexusConsole {
}
