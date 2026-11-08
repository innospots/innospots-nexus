package com.innospots.nexus.console.role.operator;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.domain.ownership.ConsoleOwnership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 配置声明的只读角色不允许在管理端删除、变更或停用。
 *
 * <p>只读标记由 {@code console/bootstrap/roles.yaml} 同步写入，
 * 只能通过修改配置文件并重启来变更。</p>
 */
class RoleOperatorReadOnlyTest {

    private static final ConsoleOwnership PLATFORM =
            new ConsoleOwnership(RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());

    private final RoleDao roleDao = mock(RoleDao.class);
    private final RoleBindingDao bindingDao = mock(RoleBindingDao.class);
    private final RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);

    @Test
    void updateRejectsReadOnlyRole() {
        when(roleDao.selectOne(any())).thenReturn(readOnlyRole());
        when(bindingDao.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> operator.updateRole(
                "ROL00000000001", new RoleUpdateRequest("新名称", null, null), PLATFORM))
                .isInstanceOf(NexusException.class)
                .extracting(exception -> ((NexusException) exception).code())
                .isEqualTo(RoleStatusCode.ROLE_READ_ONLY.fullCode());

        verify(roleDao, never()).updateById(any(RoleEntity.class));
    }

    @Test
    void statusChangeRejectsReadOnlyRole() {
        when(roleDao.selectOne(any())).thenReturn(readOnlyRole());
        when(bindingDao.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> operator.updateRoleStatus(
                "ROL00000000001", new RoleStatusUpdateRequest(BasicStatus.DISABLED), PLATFORM))
                .isInstanceOf(NexusException.class)
                .extracting(exception -> ((NexusException) exception).code())
                .isEqualTo(RoleStatusCode.ROLE_READ_ONLY.fullCode());

        verify(roleDao, never()).updateById(any(RoleEntity.class));
    }

    @Test
    void deleteRejectsReadOnlyRole() {
        when(roleDao.selectOne(any())).thenReturn(readOnlyRole());
        when(bindingDao.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> operator.deleteRole("ROL00000000001", PLATFORM))
                .isInstanceOf(NexusException.class)
                .extracting(exception -> ((NexusException) exception).code())
                .isEqualTo(RoleStatusCode.ROLE_READ_ONLY.fullCode());

        verify(roleDao, never()).deleteById(anyString());
    }

    @Test
    void writableRoleCanStillBeUpdated() {
        RoleEntity role = readOnlyRole();
        role.setReadOnly(false);
        when(roleDao.selectOne(any())).thenReturn(role);
        when(bindingDao.selectCount(any())).thenReturn(0L);

        operator.updateRole(
                "ROL00000000001", new RoleUpdateRequest("普通管理员", null, null), PLATFORM);

        assertThat(role.getRoleName()).isEqualTo("普通管理员");
        verify(roleDao).updateById(role);
    }

    private RoleEntity readOnlyRole() {
        RoleEntity role = new RoleEntity();
        role.setRoleId("ROL00000000001");
        role.setRoleName("系统管理员");
        role.setRoleCode("sys_admin");
        role.setOwnerType(RoleOwnerType.PLATFORM.name());
        role.setSecurityRealm(SecurityRealm.PLATFORM.name());
        role.setStatus(BasicStatus.ENABLED.name());
        role.setSortOrder(10);
        role.setBuiltIn(true);
        role.setAdministrator(true);
        role.setReadOnly(true);
        return role;
    }
}
