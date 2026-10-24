package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.permission.authorization.AuthorizationContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestMocks;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestTokens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleResourcePermissionFilterTest extends ConsoleJaxRsWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TokenIssuer tokenIssuer;

    private String accessToken;

    @BeforeEach
    void resetAuthorizerMock() {
        Mockito.reset(ConsoleJaxRsWebTestMocks.RESOURCE_PERMISSION_AUTHORIZER);
        accessToken = ConsoleJaxRsWebTestTokens.platformAccessToken(tokenIssuer);
    }

    @Test
    void pathOutsideConsolePatternsSkipsAuthorizer() throws Exception {
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/jaxrs-web-test/secured-echo",
                null,
                accessToken);
        assertThat(response.statusCode()).isEqualTo(200);
        verify(ConsoleJaxRsWebTestMocks.RESOURCE_PERMISSION_AUTHORIZER, never())
                .authorizeEndpoint(anyString(), any(ResolvedEndpointGrant.class), any());
    }

    @Test
    void grantedApiPathWithDeniedDecisionReturns403() throws Exception {
        when(ConsoleJaxRsWebTestMocks.RESOURCE_PERMISSION_AUTHORIZER.authorizeEndpoint(
                        anyString(), any(ResolvedEndpointGrant.class), any(AuthorizationSubject.class)))
                .thenReturn(AuthorizationDecision.deny("denied"));
        HttpResponse<String> response = http.exchange(
                "GET",
                "/api/d/platform/jaxrs-web-test/secured-echo",
                null,
                accessToken);
        assertThat(response.statusCode()).isEqualTo(403);
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isFalse();
    }

    @Test
    void grantedApiPathWithAllowedDecisionSucceeds() throws Exception {
        when(ConsoleJaxRsWebTestMocks.RESOURCE_PERMISSION_AUTHORIZER.authorizeEndpoint(
                        anyString(), any(ResolvedEndpointGrant.class), any(AuthorizationSubject.class)))
                .thenReturn(AuthorizationDecision.allow(new AuthorizationContext(
                        "wks-demo",
                        "platform.jaxrs-web-test:jaxrs.web.test.echo",
                        null,
                        List.of())));
        HttpResponse<String> response = http.exchange(
                "GET",
                "/api/d/platform/jaxrs-web-test/secured-echo",
                null,
                accessToken);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("granted-api");
    }
}
