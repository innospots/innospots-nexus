package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.service.GrantSubjectAccess;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.role.dao.RoleDao;

/**
 * {@code console.permission} 域 Quarkus CDI 装配（授权授予；可见性与主体解析见 {@link
 * com.innospots.nexus.quarkus.console.bootstrap.ConsoleCatalogBeans}）。
 */
@ApplicationScoped
public class ConsolePermissionBeans {

    @Produces
    @Singleton
    GrantSubjectAccess grantSubjectAccess(RoleDao roleDao) {
        return new GrantSubjectAccess(roleDao, null);
    }

    @Produces
    @Singleton
    PermissionGrantService permissionGrantService(
            PermissionGrantDao grantDao,
            ConsoleCatalogResourceDao resourceDao,
            GrantSubjectAccess grantSubjectAccess) {
        return new PermissionGrantService(grantDao, resourceDao, grantSubjectAccess);
    }
}
