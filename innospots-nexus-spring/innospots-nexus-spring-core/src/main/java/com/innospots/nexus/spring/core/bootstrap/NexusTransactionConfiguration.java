package com.innospots.nexus.spring.core.bootstrap;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.JtaTransactionAnnotationParser;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * 启用 Spring 声明式事务，并解析 console 操作类上的 {@code jakarta.transaction.Transactional}。
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class NexusTransactionConfiguration {

    /**
     * 在单数据源 {@code DataSourceTransactionManager} 场景下仍识别 Jakarta {@code Transactional}。
     *
     * <p>{@link Role#ROLE_INFRASTRUCTURE} 避免在部分 {@code BeanPostProcessor} 创建阶段被提前拉起而触发告警。</p>
     */
    @Bean
    @Primary
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static TransactionAttributeSource nexusTransactionAttributeSource() {
        return new AnnotationTransactionAttributeSource(
                new SpringTransactionAnnotationParser(),
                new JtaTransactionAnnotationParser());
    }
}
