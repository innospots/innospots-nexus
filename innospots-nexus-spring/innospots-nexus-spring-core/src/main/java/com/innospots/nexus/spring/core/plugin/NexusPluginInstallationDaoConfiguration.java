package com.innospots.nexus.spring.core.plugin;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Core 插件安装域 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-core}，对应 {@code innospots-nexus-core}
 * 的 {@code plugin.installation.dao} 包。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.core.plugin.installation.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class NexusPluginInstallationDaoConfiguration {
}

