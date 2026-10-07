package com.innospots.nexus.console.jaxrs.filter;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebDevSessionSettings;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleDevSessionFilterTest {

    @AfterEach
    void tearDown() {
        JaxRsFilterTestSupport.clearThreadState();
    }

    @Test
    void skipsWhenSecurityEnabled() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(true);
        ConsoleDevSessionFilter filter = new ConsoleDevSessionFilter(security);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status");

        filter.filter(request);

        assertThat(SessionContext.user()).isEmpty();
    }

    @Test
    void skipsWhenDevSessionDisabled() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        security.getDevSession().setEnabled(false);
        ConsoleDevSessionFilter filter = new ConsoleDevSessionFilter(security);

        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status"));

        assertThat(SessionContext.user()).isEmpty();
    }

    @Test
    void bindsDevSessionWhenSecurityOffAndDevSessionEnabled() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        ConsoleWebDevSessionSettings devSession = security.getDevSession();
        devSession.setEnabled(true);
        devSession.setUserId("dev-user");
        devSession.setTenantId("dev-tenant");
        devSession.setWorkspaceId("dev-workspace");

        ConsoleDevSessionFilter filter = new ConsoleDevSessionFilter(security);
        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status"));

        assertThat(SessionContext.user()).isPresent();
        assertThat(SessionContext.user().orElseThrow().userName()).isEqualTo("dev-user");
        assertThat(SessionContext.workspace()).isPresent();
    }

    @Test
    void doesNotOverrideExistingUser() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        security.getDevSession().setEnabled(true);
        SessionContext.bindUser(UserSnapshot.simple(99L, "existing", null));

        ConsoleDevSessionFilter filter = new ConsoleDevSessionFilter(security);
        filter.filter(JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status"));

        assertThat(SessionContext.user().orElseThrow().userName()).isEqualTo("existing");
    }
}
