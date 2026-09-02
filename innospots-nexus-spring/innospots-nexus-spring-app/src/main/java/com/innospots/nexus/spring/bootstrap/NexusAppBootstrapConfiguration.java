package com.innospots.nexus.spring.bootstrap;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.innospots.nexus.core.persistence.handler.AuditMetaObjectHandler;

/**
 * 应用服务启动引导装配。
 *
 * <p>扫描 Core Mapper 并注册 MyBatis-Plus 默认行为；数据源、端口与 DDL 由
 * {@code application.yaml} 提供。</p>
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.core",
        annotationClass = Mapper.class)
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
            properties.getConfiguration().setMapUnderscoreToCamelCase(true);
            properties.getGlobalConfig().getDbConfig().setIdType(IdType.ASSIGN_UUID);
            properties.getGlobalConfig().setMetaObjectHandler(auditMetaObjectHandler);
        };
    }
}
