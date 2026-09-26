package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "nexus.console.web.security.enabled=false",
        "nexus.console.web.security.dev-session.enabled=true",
        "nexus.console.web.security.dev-session.user-id=9001",
        "nexus.console.web.security.dev-session.tenant-id=tenant-dev",
        "nexus.console.web.security.dev-session.workspace-id=wks-dev"
})
class ConsoleWebSecurityDisabledTest extends ConsoleJaxRsWebIntegrationTest {

    @Test
    void securedPathWithoutTokenSucceedsWhenSecurityDisabled() throws Exception {
        HttpResponse<String> response = http.get("/console/jaxrs-web-test/secured-echo");
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void consolePathWithoutPageKeySucceedsWhenSecurityDisabled() throws Exception {
        HttpResponse<String> response = http.get("/console/datasource/demo");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("datasource");
    }

    @Test
    void devSessionBindsConfiguredUserWhenSecurityDisabled() throws Exception {
        HttpResponse<String> response = http.get("/console/jaxrs-web-test/session-user-id");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("9001");
    }
}
