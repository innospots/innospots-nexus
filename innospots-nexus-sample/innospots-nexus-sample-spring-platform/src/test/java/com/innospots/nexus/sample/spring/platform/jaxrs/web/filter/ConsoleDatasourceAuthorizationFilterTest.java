package com.innospots.nexus.sample.spring.platform.jaxrs.web.filter;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.permission.authorization.AuthorizationContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationRequest;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebIntegrationTest;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestMocks;
import com.innospots.nexus.sample.spring.platform.jaxrs.web.support.ConsoleJaxRsWebTestTokens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleDatasourceAuthorizationFilterTest extends ConsoleJaxRsWebIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TokenIssuer tokenIssuer;

    private String accessToken;

    @BeforeEach
    void resetAuthorizerMock() {
        Mockito.reset(ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER);
        accessToken = ConsoleJaxRsWebTestTokens.platformAccessToken(tokenIssuer);
    }

    @Test
    void nonDatasourcePathSkipsAuthorizer() throws Exception {
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/jaxrs-web-test/secured-echo",
                null,
                accessToken);
        assertThat(response.statusCode()).isEqualTo(200);
        verify(ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER, never()).authorize(any());
    }

    @Test
    void datasourcePathWithoutPageKeyReturns403() throws Exception {
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/datasource/demo",
                null,
                accessToken);
        assertThat(response.statusCode()).isEqualTo(403);
        verify(ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER, never()).authorize(any());
    }

    @Test
    void datasourcePathWithDeniedDecisionReturns403() throws Exception {
        when(ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER.authorize(any(AuthorizationRequest.class)))
                .thenReturn(AuthorizationDecision.deny("denied"));
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/datasource/demo",
                Map.of("X-Nexus-Page-Key", "demo.page"),
                accessToken);
        assertThat(response.statusCode()).isEqualTo(403);
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        assertThat(root.get("success").asBoolean()).isFalse();
    }

    @Test
    void datasourcePathWithAllowedDecisionSucceeds() throws Exception {
        when(ConsoleJaxRsWebTestMocks.REQUEST_AUTHORIZER.authorize(any(AuthorizationRequest.class)))
                .thenReturn(AuthorizationDecision.allow(new AuthorizationContext(
                        "wks-demo",
                        "demo.page",
                        "orders",
                        List.of())));
        HttpResponse<String> response = http.exchange(
                "GET",
                "/console/datasource/demo",
                Map.of("X-Nexus-Page-Key", "demo.page"),
                accessToken);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("datasource");
    }
}
