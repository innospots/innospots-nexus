package com.innospots.nexus.spring.dictionary.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Console 字典域 DAO 扫描。
 *
 * <p>归属 {@code innospots-nexus-spring-console}，对应 {@code innospots-nexus-console}
 * 的 {@code dictionary.dao} 包。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.dictionary.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleDictionaryDaoConfiguration {
}
