package com.innospots.nexus.console.bootstrap.service;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.ModuleGrant;
import com.innospots.nexus.console.bootstrap.loader.RoleAuthorizationManifestLoader;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
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
 * {@link RoleGrantSyncService} 授权同步语义测试（启动引导第 3 阶段）。
 */
class RoleGrantSyncServiceTest {

    private final RoleDao roleDao = mock(RoleDao.class);
    private final PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
    private final ConsoleCatalogService catalogService = mock(ConsoleCatalogService.class);

    private final RoleDefinitionSyncService roleService = new RoleDefinitionSyncService(roleDao);
    private final RoleGrantSyncService service =
            new RoleGrantSyncService(roleService, grantDao, catalogService);

    @Test
    void createsReadOnlyGrant() {
        when(roleDao.selectOne(any())).thenReturn(role());
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.logger:view", PermissionGrantKeyType.MODULE));
        when(grantDao.selectOne(any())).thenReturn(null);

        RoleGrantSyncService.SyncResult result = service.sync(List.of(
                definition(new ModuleGrant("system", "logger", List.of("view")))));

        ArgumentCaptor<PermissionGrantEntity> captor =
                ArgumentCaptor.forClass(PermissionGrantEntity.class);
        verify(grantDao).insert(captor.capture());
        PermissionGrantEntity grant = captor.getValue();
        assertThat(grant.getGrantKey()).isEqualTo("system.logger:view");
        assertThat(grant.getGrantKeyType()).isEqualTo(PermissionGrantKeyType.MODULE.code());
        assertThat(grant.getSubjectType()).isEqualTo("ROLE");
        assertThat(grant.getSubjectId()).isEqualTo("ROL00000000009");
        assertThat(grant.getReadOnly()).isTrue();
        assertThat(result.created()).isEqualTo(1);
    }

    @Test
    void marksExistingGrantReadOnly() {
        PermissionGrantEntity existing = new PermissionGrantEntity();
        existing.setGrantId("PGR00000000001");
        existing.setGrantKey("system.logger:view");
        existing.setGrantKeyType(PermissionGrantKeyType.MODULE.code());
        existing.setReadOnly(false);
        when(roleDao.selectOne(any())).thenReturn(role());
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.logger:view", PermissionGrantKeyType.MODULE));
        when(grantDao.selectOne(any())).thenReturn(existing);

        RoleGrantSyncService.SyncResult result = service.sync(List.of(
                definition(new ModuleGrant("system", "logger", List.of("view")))));

        verify(grantDao).updateById(existing);
        assertThat(existing.getReadOnly()).isTrue();
        assertThat(result.updated()).isEqualTo(1);
    }

    @Test
    void doesNotDeleteGrantsMissingFromConfiguration() {
        when(roleDao.selectOne(any())).thenReturn(role());
        when(catalogService.grantKeyTypes()).thenReturn(Map.of());

        service.sync(List.of(definition()));

        verify(grantDao, never()).deleteById(any(java.io.Serializable.class));
        verify(grantDao, never()).delete(any());
    }

    @Test
    void failsOnUnknownPermissionResource() {
        when(roleDao.selectOne(any())).thenReturn(role());
        // 目录为空：模拟第 1 阶段未执行或资源键不存在。
        when(catalogService.grantKeyTypes()).thenReturn(Map.of());

        List<RoleDefinition> roles = List.of(
                definition(new ModuleGrant("system", "not-exist", List.of("view"))));

        assertThatThrownBy(() -> service.sync(roles))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("system.not-exist:view");
    }

    @Test
    void skipsWhenRoleMissing() {
        when(roleDao.selectOne(any())).thenReturn(null);
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.logger:view", PermissionGrantKeyType.MODULE));

        RoleGrantSyncService.SyncResult result = service.sync(List.of(
                definition(new ModuleGrant("system", "logger", List.of("view")))));

        verify(grantDao, never()).insert(any(PermissionGrantEntity.class));
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    void expandsModuleGrantIntoMultipleKeys() {
        List<String> keys = RoleAuthorizationManifestLoader.expandGrantKeys(List.of(
                new ModuleGrant("system", "settings", List.of("view", "operate", "update"))));

        assertThat(keys).containsExactly(
                "system.settings:view", "system.settings:operate", "system.settings:update");
    }

    @Test
    void insertsOneGrantPerGroupInList() {
        when(roleDao.selectOne(any())).thenReturn(role());
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.settings:view", PermissionGrantKeyType.MODULE,
                "system.settings:update", PermissionGrantKeyType.MODULE));
        when(grantDao.selectOne(any())).thenReturn(null);

        RoleGrantSyncService.SyncResult result = service.sync(List.of(
                definition(new ModuleGrant("system", "settings", List.of("view", "update")))));

        assertThat(result.created()).isEqualTo(2);
    }

    private RoleDefinition definition(ModuleGrant... grants) {
        return new RoleDefinition(
                "auditor", "审计员", null, null, "PLATFORM", false, 40, List.of(grants));
    }

    private RoleEntity role() {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId("ROL00000000009");
        entity.setRoleCode("auditor");
        entity.setOwnerType("PLATFORM");
        entity.setSecurityRealm("PLATFORM");
        return entity;
    }
}
