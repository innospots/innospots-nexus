package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;

/**
 * {@code console.role} 域 Quarkus CDI 装配（operator；生命周期端点由业务域实现）。
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
}
