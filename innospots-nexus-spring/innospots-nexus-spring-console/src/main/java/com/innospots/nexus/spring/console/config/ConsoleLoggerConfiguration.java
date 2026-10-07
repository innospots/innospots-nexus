package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.logger.InvocationLogHandler;
import com.innospots.nexus.console.logger.LogExecutor;
import com.innospots.nexus.console.logger.dao.AuditLogDao;
import com.innospots.nexus.console.logger.handler.PersistenceInvocationLogHandler;
import com.innospots.nexus.console.logger.operator.InvocationLogOperator;

/**
 * {@code console.logger} 域 Spring 装配：调用审计日志持久化与异步执行器。
 *
 * @author Smars
 * @date 2026/09/23
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.logger.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleLoggerConfiguration {

    @Bean
    InvocationLogOperator invocationLogOperator(AuditLogDao auditLogDao) {
        return new InvocationLogOperator(auditLogDao);
    }

    @Bean
    InvocationLogHandler invocationLogHandler(InvocationLogOperator invocationLogOperator) {
        return new PersistenceInvocationLogHandler(invocationLogOperator);
    }

    @Bean
    LogExecutor logExecutor(InvocationLogHandler invocationLogHandler) {
        return new LogExecutor(invocationLogHandler);
    }
}
