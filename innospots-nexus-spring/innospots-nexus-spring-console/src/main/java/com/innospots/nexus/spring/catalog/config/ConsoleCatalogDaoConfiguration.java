package com.innospots.nexus.spring.catalog.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Console 目录索引 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-console}，对应 {@code innospots-nexus-core}
 * 的 {@code plugin.contribution.console.catalog.dao} 包（宿主级目录表
 * {@code nx_console_catalog_resource}）。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.core.plugin.contribution.console.catalog.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleCatalogDaoConfiguration {
}
