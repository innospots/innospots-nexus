package com.innospots.nexus.spring.bootstrap;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.innospots.nexus.core.persistence.handler.AuditMetaObjectHandler;

/**
 * 应用服务启动引导装配。
 *
 * <p>扫描 Core Mapper 并注册 MyBatis-Plus 默认行为；数据源、端口与 DDL 由
 * {@code application.yaml} 提供。</p>
 */
@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class})
@MapperScan(
        basePackages = "com.innospots.nexus.core.plugin.installation.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class NexusAppBootstrapConfiguration {

    /**
     * MyBatis-Plus 审计字段自动填充处理器。
     */
    @Bean
    AuditMetaObjectHandler auditMetaObjectHandler() {
        return new AuditMetaObjectHandler();
    }

    /**
     * MyBatis-Plus 默认 ORM 行为（可被 {@code application.yaml} 覆盖）。
     */
    @Bean
    MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer(AuditMetaObjectHandler auditMetaObjectHandler) {
        return properties -> {
            if (properties.getConfiguration() == null) {
                properties.setConfiguration(new MybatisPlusProperties.CoreConfiguration());
            }
            properties.getConfiguration().setMapUnderscoreToCamelCase(true);
            if (properties.getGlobalConfig() == null) {
                properties.setGlobalConfig(new GlobalConfig());
            }
            if (properties.getGlobalConfig().getDbConfig() == null) {
                properties.getGlobalConfig().setDbConfig(new GlobalConfig.DbConfig());
            }
            properties.getGlobalConfig().getDbConfig().setIdType(IdType.ASSIGN_UUID);
            properties.getGlobalConfig().setMetaObjectHandler(auditMetaObjectHandler);
        };
    }
}
