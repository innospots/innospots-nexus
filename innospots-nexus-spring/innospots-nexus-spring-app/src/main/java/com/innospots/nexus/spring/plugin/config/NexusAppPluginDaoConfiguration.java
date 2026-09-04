package com.innospots.nexus.spring.plugin.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Core 插件安装域 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-app}，对应 {@code innospots-nexus-core}
 * 的 {@code plugin.installation.dao} 包。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.core.plugin.installation.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class NexusAppPluginDaoConfiguration {
}
