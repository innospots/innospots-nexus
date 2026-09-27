package com.innospots.nexus.spring.core.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
     */
    @Bean
    @Primary
    static TransactionAttributeSource nexusTransactionAttributeSource() {
        return new AnnotationTransactionAttributeSource(
                new SpringTransactionAnnotationParser(),
                new JtaTransactionAnnotationParser());
    }
}
