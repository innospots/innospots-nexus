package com.innospots.nexus.console.bootstrap.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RoleDefinitionSyncService} 角色同步语义测试（启动引导第 2 阶段）。
 */
class RoleDefinitionSyncServiceTest {

    private final RoleDao roleDao = mock(RoleDao.class);
    private final RoleDefinitionSyncService service = new RoleDefinitionSyncService(roleDao);

    @Test
    void createsReadOnlyEnabledRole() {
        when(roleDao.selectOne(any())).thenReturn(null);

        RoleDefinitionSyncService.SyncResult result = service.sync(List.of(
                new RoleDefinition("auditor", "审计员", null, "只读审计", "PLATFORM", false, 40, null)));

        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleDao).insert(captor.capture());
        RoleEntity role = captor.getValue();
        assertThat(role.getRoleCode()).isEqualTo("auditor");
        assertThat(role.getRoleName()).isEqualTo("审计员");
        assertThat(role.getOwnerType()).isEqualTo("PLATFORM");
        assertThat(role.getSecurityRealm()).isEqualTo("PLATFORM");
        assertThat(role.getOwnerId()).isNull();
        assertThat(role.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        assertThat(role.getBuiltIn()).isTrue();
        assertThat(role.getReadOnly()).isTrue();
        assertThat(role.getSortOrder()).isEqualTo(40);
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isZero();
    }

    @Test
    void marksExistingRoleReadOnlyAndUpdatesChangedFields() {
        RoleEntity existing = role("auditor", "旧名称", BasicStatus.DISABLED.name(), 1, false);
        when(roleDao.selectOne(any())).thenReturn(existing);

        RoleDefinitionSyncService.SyncResult result = service.sync(List.of(
                new RoleDefinition("auditor", "审计员", null, null, "PLATFORM", false, 40, null)));

        verify(roleDao).updateById(existing);
        assertThat(existing.getRoleName()).isEqualTo("审计员");
        assertThat(existing.getSortOrder()).isEqualTo(40);
        assertThat(existing.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        assertThat(existing.getReadOnly()).isTrue();
        assertThat(result.updated()).isEqualTo(1);
    }

    @Test
    void skipsUpdateWhenDefinitionAlreadyMatches() {
        RoleEntity existing = role("auditor", "审计员", BasicStatus.ENABLED.name(), 40, true);
        when(roleDao.selectOne(any())).thenReturn(existing);

        RoleDefinitionSyncService.SyncResult result = service.sync(List.of(
                new RoleDefinition("auditor", "审计员", null, null, "PLATFORM", false, 40, null)));

        verify(roleDao, never()).updateById(any(RoleEntity.class));
        assertThat(result.updated()).isZero();
    }

    @Test
    void doesNotDeleteRolesMissingFromConfiguration() {
        when(roleDao.selectOne(any())).thenReturn(
                role("auditor", "审计员", BasicStatus.ENABLED.name(), 40, true));

        service.sync(List.of(
                new RoleDefinition("auditor", "审计员", null, null, "PLATFORM", false, 40, null)));

        verify(roleDao, never()).deleteById(any(java.io.Serializable.class));
        verify(roleDao, never()).delete(any());
    }

    @Test
    void failsOnInvalidOwnerType() {
        List<RoleDefinition> roles = List.of(
                new RoleDefinition("auditor", "审计员", null, null, "GLOBAL", false, 1, null));

        assertThatThrownBy(() -> service.sync(roles))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("GLOBAL");
    }

    private RoleEntity role(String code, String name, String status, int sortOrder, boolean readOnly) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId("ROL00000000009");
        entity.setRoleCode(code);
        entity.setRoleName(name);
        entity.setOwnerType("PLATFORM");
        entity.setSecurityRealm("PLATFORM");
        entity.setStatus(status);
        entity.setSortOrder(sortOrder);
        entity.setAdministrator(false);
        entity.setReadOnly(readOnly);
        return entity;
    }
}
