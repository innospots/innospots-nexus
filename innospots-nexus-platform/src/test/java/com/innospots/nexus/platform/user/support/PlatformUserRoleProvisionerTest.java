package com.innospots.nexus.platform.user.support;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformUserRoleProvisionerTest {

    @Test
    void assignDefaultRolesIfPresentSkipsWhenBlank() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        PlatformUserRoleProvisioner provisioner = new PlatformUserRoleProvisioner(roleDao, roleBindingDao);

        provisioner.assignDefaultRolesIfPresent("usr-1", "  ");
        provisioner.assignDefaultRolesIfPresent("usr-1", null);

        verify(roleDao, never()).selectOne(any());
    }

    @Test
    void assignDefaultRolesIfPresentBindsUserWhenRoleExists() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        PlatformUserRoleProvisioner provisioner = new PlatformUserRoleProvisioner(roleDao, roleBindingDao);

        RoleEntity role = new RoleEntity();
        role.setRoleId("rol-1");
        role.setRoleCode("ops");
        role.setOwnerType(RoleOwnerType.PLATFORM.name());
        role.setSecurityRealm(SecurityRealm.PLATFORM.name());
        role.setStatus(BasicStatus.ENABLED.name());
        when(roleDao.selectOne(any())).thenReturn(role);
        when(roleBindingDao.selectCount(any())).thenReturn(0L);

        provisioner.assignDefaultRolesIfPresent("usr-1", "ops");

        verify(roleBindingDao).insert(any(RoleBindingEntity.class));
    }

    @Test
    void assignDefaultRolesIfPresentRejectsUnknownRoleCode() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        PlatformUserRoleProvisioner provisioner = new PlatformUserRoleProvisioner(roleDao, roleBindingDao);
        when(roleDao.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> provisioner.assignDefaultRolesIfPresent("usr-1", "missing"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformInviteStatusCode.INVITE_DEFAULT_ROLE_NOT_FOUND.fullCode());
    }
}
