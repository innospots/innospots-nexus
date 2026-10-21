package com.innospots.nexus.console.permission.pageaccess;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

class GrantGroupPageAccessVerifierTest {

    @Test
    void allowsWhenPageAuthorizerAllows() {
        ConsolePagePermissionAuthorizer authorizer = mock(ConsolePagePermissionAuthorizer.class);
        when(authorizer.authorize(any())).thenReturn(AuthorizationDecision.allow(null));
        PageAccessVerifier verifier = new GrantGroupPageAccessVerifier(authorizer);

        PageAccessVerifyResult result = verifier.verify(
                new PageAccessVerifyRequest("platform", "user", "platform-user-main", "default"),
                "wks01",
                new AuthorizationSubject("u1", Set.of(), Set.of(), false));

        assertThat(result.allowed()).isTrue();
    }
}
