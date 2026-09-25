package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.role.service.RoleService;

/**
 * {@code console.role} 域 Quarkus CDI 装配。
 */
@ApplicationScoped
public class ConsoleRoleBeans {

    @Produces
    @Singleton
    RoleConverter roleConverter() {
        return RoleConverter.INSTANCE;
    }

    @Produces
    @Singleton
    RoleOperator roleOperator(RoleDao roleDao, RoleBindingDao roleBindingDao, RoleConverter roleConverter) {
        return new RoleOperator(roleDao, roleBindingDao, roleConverter);
    }

    @Produces
    @Singleton
    RoleBindingOperator roleBindingOperator(
            RoleOperator roleOperator,
            RoleBindingDao roleBindingDao,
            RoleConverter roleConverter) {
        return new RoleBindingOperator(roleOperator, roleBindingDao, roleConverter);
    }

    @Produces
    @Singleton
    RoleService roleService(RoleOperator roleOperator, RoleBindingOperator roleBindingOperator) {
        return new RoleService(roleOperator, roleBindingOperator);
    }
}
