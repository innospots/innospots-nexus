package com.innospots.nexus.console.permission.pageaccess;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;

class GrantGroupPageAccessVerifierTest {

    @Test
    void allowsAdministratorWithoutGrantRows() {
        PageAccessVerifier verifier = new GrantGroupPageAccessVerifier(mock(PermissionGrantDao.class));

        PageAccessVerifyResult result = verifier.verify(
                new PageAccessVerifyRequest("platform", "user", "platform-user-main", "default"),
                "wks01",
                new AuthorizationSubject("u1", Set.of(), Set.of(), true));

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void allowsWhenSubjectHasModuleScopedGrant() {
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        PermissionGrantEntity grant = new PermissionGrantEntity();
        grant.setGrantKey("platform.user.create:platform.user");
        when(grantDao.selectList(any())).thenReturn(List.of(grant));
        PageAccessVerifier verifier = new GrantGroupPageAccessVerifier(grantDao);

        PageAccessVerifyResult result = verifier.verify(
                new PageAccessVerifyRequest("platform", "user", "platform-user-main", "default"),
                "wks01",
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false));

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void deniesWhenSubjectHasNoModuleScopedGrant() {
        PermissionGrantDao grantDao = mock(PermissionGrantDao.class);
        when(grantDao.selectList(any())).thenReturn(List.of());
        PageAccessVerifier verifier = new GrantGroupPageAccessVerifier(grantDao);

        PageAccessVerifyResult result = verifier.verify(
                new PageAccessVerifyRequest("platform", "user", "platform-user-main", "default"),
                "wks01",
                new AuthorizationSubject("u1", Set.of("role-1"), Set.of(), false));

        assertThat(result.allowed()).isFalse();
    }
}
