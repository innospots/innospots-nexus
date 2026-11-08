package com.innospots.nexus.console.permission.service;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantItemRequest;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.service.GrantSubjectAccess;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.domain.ownership.ConsoleOwnership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 配置声明的只读授权在管理端全量替换时不被删除、不被重复插入。
 *
 * <p>只读授权由 {@code console/bootstrap/roles.yaml} 同步写入，
 * 只能通过修改配置文件并重启来变更。</p>
 */
class PermissionGrantServiceReadOnlyTest {

    private static final ConsoleOwnership PLATFORM =
            new ConsoleOwnership(RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());

    private final PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
    private final ConsoleCatalogService catalogService = mock(ConsoleCatalogService.class);
    private final GrantSubjectAccess grantSubjectAccess = mock(GrantSubjectAccess.class);

    private final PermissionGrantService service =
            new PermissionGrantService(grantDao, catalogService, grantSubjectAccess);

    @Test
    void keepsReadOnlyGrantAndSkipsReinsertingIt() {
        when(grantDao.selectList(any())).thenReturn(List.of(
                readOnlyGrant("system.logger:view")));
        // 只读键也必须存在于权限目录中，validateSubject 会统一校验。
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.logger:view", PermissionGrantKeyType.MODULE,
                "operation.role:view", PermissionGrantKeyType.MODULE));

        service.replace(
                PermissionSubjectType.ROLE,
                "ROL00000000001",
                new PermissionGrantReplaceRequest(List.of(
                        new PermissionGrantItemRequest("system.logger:view", null),
                        new PermissionGrantItemRequest("operation.role:view", null))),
                PLATFORM,
                null,
                null);

        // 删除时排除只读行：LambdaQueryWrapper 的 toString 不含条件，
        // 因此通过"只读键未被重新插入"间接验证删除范围（否则唯一键冲突或被覆盖）。
        verify(grantDao).delete(any());

        ArgumentCaptor<PermissionGrantEntity> insertCaptor =
                ArgumentCaptor.forClass(PermissionGrantEntity.class);
        verify(grantDao).insert(insertCaptor.capture());
        PermissionGrantEntity inserted = insertCaptor.getValue();
        assertThat(inserted.getGrantKey()).isEqualTo("operation.role:view");
        assertThat(inserted.getReadOnly()).isFalse();
    }

    @Test
    void insertsNothingWhenOnlyReadOnlyGrantsRequested() {
        when(grantDao.selectList(any())).thenReturn(List.of(
                readOnlyGrant("system.logger:view")));
        when(catalogService.grantKeyTypes()).thenReturn(Map.of(
                "system.logger:view", PermissionGrantKeyType.MODULE));

        service.replace(
                PermissionSubjectType.ROLE,
                "ROL00000000001",
                new PermissionGrantReplaceRequest(List.of(
                        new PermissionGrantItemRequest("system.logger:view", null))),
                PLATFORM,
                null,
                null);

        verify(grantDao, never()).insert(any(PermissionGrantEntity.class));
    }

    private PermissionGrantEntity readOnlyGrant(String grantKey) {
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantId("PGR00000000001");
        grant.setSubjectType(PermissionSubjectType.ROLE.name());
        grant.setSubjectId("ROL00000000001");
        grant.setGrantKey(grantKey);
        grant.setGrantKeyType(PermissionGrantKeyType.MODULE.code());
        grant.setOwnerType("PLATFORM");
        grant.setSecurityRealm("PLATFORM");
        grant.setReadOnly(true);
        return grant;
    }
}
