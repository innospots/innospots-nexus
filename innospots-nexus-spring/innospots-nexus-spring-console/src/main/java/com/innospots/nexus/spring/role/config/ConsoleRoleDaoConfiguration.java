package com.innospots.nexus.spring.role.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Console 角色域 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-console}，对应 {@code innospots-nexus-console}
 * 的 {@code role.dao} 包。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.role.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleRoleDaoConfiguration {
}
