package com.innospots.nexus.spring.permission.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Console 权限域 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-console}，对应 {@code innospots-nexus-console}
 * 的 {@code permission.dao} 包（workspace 级授权表 {@code nx_permission_grant}）。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.permission.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsolePermissionDaoConfiguration {
}
