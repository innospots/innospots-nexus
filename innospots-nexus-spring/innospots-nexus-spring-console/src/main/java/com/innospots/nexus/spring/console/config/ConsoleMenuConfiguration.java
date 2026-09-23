package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * {@code console.menu} 域 Spring 装配。
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.menu.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleMenuConfiguration {
}
