package com.innospots.nexus.platform.auth.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformAuthServiceTest {

    @Test
    void loginDelegatesToAuthFacade() {
        AuthFacade authFacade = mock(AuthFacade.class);
        PlatformAuthService service = new PlatformAuthService(authFacade);

        AuthLoginRequest request = new AuthLoginRequest("admin", "enc", null, null);
        AuthTokenVo token = new AuthTokenVo(
                SecurityRealm.PLATFORM, "BUSINESS", "access", "refresh", null, null, null, null);
        when(authFacade.login(request)).thenReturn(token);

        assertThat(service.login(request)).isSameAs(token);
        verify(authFacade).login(request);
    }
}
