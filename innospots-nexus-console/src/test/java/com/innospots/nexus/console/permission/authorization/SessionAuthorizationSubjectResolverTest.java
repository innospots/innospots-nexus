package com.innospots.nexus.console.permission.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionAuthorizationSubjectResolverTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void resolveUsesPlatformOwnershipWhenSessionScopeIsPlatform() {
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
        ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
        SessionContext.bindUser(UserSnapshot.simple(1L, "dev", "Dev"));

        RoleBindingEntity binding = new RoleBindingEntity();
        binding.setRoleId("rol-1");
        RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);
        when(roleBindingDao.selectList(any())).thenReturn(java.util.List.of(binding));

        SessionAuthorizationSubjectResolver resolver =
                new SessionAuthorizationSubjectResolver(roleBindingDao, mock(RoleDao.class));

        AuthorizationSubject subject = resolver.resolve().orElseThrow();

        assertThat(subject.userId()).isEqualTo("1");
        assertThat(subject.roleIds()).containsExactly("rol-1");
    }
}
