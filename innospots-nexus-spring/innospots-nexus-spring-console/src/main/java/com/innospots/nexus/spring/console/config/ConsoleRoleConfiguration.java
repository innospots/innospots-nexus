package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.role.service.RoleService;

/**
 * {@code console.role} 域 Spring 装配：角色 CRUD 与绑定 REST。
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

    @Bean
    RoleService roleService(RoleOperator roleOperator, RoleBindingOperator roleBindingOperator) {
        return new RoleService(roleOperator, roleBindingOperator);
    }

    @Bean
    @Lazy
    RoleEndpoint roleEndpoint(RoleService roleService) {
        return new RoleEndpoint(roleService);
    }

    @Bean
    @Lazy
    RoleBindingEndpoint roleBindingEndpoint(RoleService roleService) {
        return new RoleBindingEndpoint(roleService);
    }
}
