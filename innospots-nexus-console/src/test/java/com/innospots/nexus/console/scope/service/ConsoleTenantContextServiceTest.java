package com.innospots.nexus.console.scope.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.scope.domain.model.TenantContext;
import com.innospots.nexus.console.scope.tenant.dao.TenantDao;
import com.innospots.nexus.console.scope.tenant.dao.TenantMemberDao;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantMemberEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsoleTenantContextServiceTest {

    private static final String TENANT = "tnt-a";
    private static final String USER = "TUS1";

    @Test
    void resolvesTenantWithMemberAndRoleCodes() {
        Harness h = new Harness();
        h.stubTenant();
        h.stubMember();
        h.stubRoles();

        TenantContext vo = h.service.resolve(USER, TENANT).orElseThrow();

        assertThat(vo.tenantId()).isEqualTo(TENANT);
        assertThat(vo.tenantMemberId()).isEqualTo("tmb-a");
        assertThat(vo.roleCodes()).containsExactly("tenant_admin");
    }

    @Test
    void resolvesWithoutRolesWhenNoBinding() {
        Harness h = new Harness();
        h.stubTenant();
        h.stubMember();
        when(h.roleBindingDao.selectList(any())).thenReturn(List.of());

        TenantContext vo = h.service.resolve(USER, TENANT).orElseThrow();

        assertThat(vo.roleCodes()).isEmpty();
    }

    @Test
    void returnsEmptyWhenTenantNotFound() {
        Harness h = new Harness();
        when(h.tenantDao.selectById(TENANT)).thenReturn(null);

        assertThat(h.service.resolve(USER, TENANT)).isEmpty();
    }

    @Test
    void returnsEmptyWhenUserIsNotTenantMember() {
        Harness h = new Harness();
        h.stubTenant();
        when(h.tenantMemberDao.findActiveMembership(USER, TENANT)).thenReturn(null);

        assertThat(h.service.resolve(USER, TENANT)).isEmpty();
    }

    @Test
    void returnsEmptyForBlankInput() {
        Harness h = new Harness();

        assertThat(h.service.resolve(null, TENANT)).isEmpty();
        assertThat(h.service.resolve(USER, " ")).isEmpty();
    }

    private static final class Harness {

        private final TenantDao tenantDao = mock(TenantDao.class);
        private final TenantMemberDao tenantMemberDao = mock(TenantMemberDao.class);
        private final RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        private final RoleDao roleDao = mock(RoleDao.class);
        private final ConsoleTenantContextService service =
                new ConsoleTenantContextService(tenantDao, tenantMemberDao, roleBindingDao, roleDao);

        private void stubTenant() {
            TenantEntity tenant = new TenantEntity();
            tenant.setTenantId(TENANT);
            tenant.setTenantCode("tcode");
            tenant.setTenantName("租户 A");
            tenant.setStatus("ACTIVE");
            when(tenantDao.selectById(TENANT)).thenReturn(tenant);
        }

        private void stubMember() {
            TenantMemberEntity member = new TenantMemberEntity();
            member.setTenantId(TENANT);
            member.setTenantMemberId("tmb-a");
            member.setPortalUserId(USER);
            when(tenantMemberDao.findActiveMembership(USER, TENANT)).thenReturn(member);
        }

        private void stubRoles() {
            RoleBindingEntity binding = new RoleBindingEntity();
            binding.setRoleId("rol-a");
            when(roleBindingDao.selectList(any())).thenReturn(List.of(binding));
            RoleEntity role = new RoleEntity();
            role.setRoleId("rol-a");
            role.setRoleCode("tenant_admin");
            when(roleDao.selectBatchIds(anyCollection())).thenReturn(List.of(role));
        }
    }
}
