package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;

/**
 * {@code console.role} 域 Spring 装配：operator 基础能力。
 *
 * <p>角色生命周期与绑定 REST 由业务域（如 platform）实现对应接口。</p>
 *
 * @author Smars
 * @date 2026/09/23
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.role.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleRoleConfiguration {

    @Bean
    RoleConverter roleConverter() {
        return RoleConverter.INSTANCE;
    }

    @Bean
    RoleOperator roleOperator(RoleDao roleDao, RoleBindingDao roleBindingDao, RoleConverter roleConverter) {
        return new RoleOperator(roleDao, roleBindingDao, roleConverter);
    }

    @Bean
    RoleBindingOperator roleBindingOperator(
            RoleOperator roleOperator,
            RoleBindingDao roleBindingDao,
            RoleConverter roleConverter) {
        return new RoleBindingOperator(roleOperator, roleBindingDao, roleConverter);
    }
}
