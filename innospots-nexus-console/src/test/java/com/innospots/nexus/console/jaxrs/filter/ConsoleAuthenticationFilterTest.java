package com.innospots.nexus.console.jaxrs.filter;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.jaxrs.support.ConsoleHttpHeaders;
import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleAuthenticationFilterTest {

    private TokenIssuer tokenIssuer;

    @BeforeEach
    void setUp() {
        tokenIssuer = mock(TokenIssuer.class);
    }

    @AfterEach
    void tearDown() {
        JaxRsFilterTestSupport.clearThreadState();
    }

    @Test
    void skipsWhenSecurityDisabled() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status");

        filter.filter(request);

        verify(tokenIssuer, never()).parse(anyString());
    }

    @Test
    void skipsPermitAllPathWithoutToken() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/openapi/specs");

        filter.filter(request);

        verify(tokenIssuer, never()).parse(anyString());
    }

    @Test
    void securedPathWithoutTokenFailsAuthentication() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setPermitAllPatterns(List.of("/openapi/**"));
        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/d/nexus/status");

        assertThatThrownBy(() -> filter.filter(request))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void securedPathWithValidBearerBindsSession() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setPermitAllPatterns(List.of("/openapi/**"));
        TokenClaims claims = JaxRsFilterTestSupport.validAccessClaims();
        when(tokenIssuer.parse("access-token")).thenReturn(claims);

        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                Map.of(ConsoleHttpHeaders.AUTHORIZATION, "Bearer access-token"));

        filter.filter(request);

        assertThat(SessionContext.user()).isPresent();
        verify(tokenIssuer).parse("access-token");
    }

    @Test
    void rejectsMalformedAuthorizationHeader() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setPermitAllPatterns(List.of("/openapi/**"));
        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/d/nexus/status",
                "GET",
                Map.of(ConsoleHttpHeaders.AUTHORIZATION, "Token not-bearer"));

        assertThatThrownBy(() -> filter.filter(request))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void publicApiWithoutTokenIsAnonymousWhenNotInPermitAll() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setPermitAllPatterns(List.of("/openapi/**"));
        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest("/api/public/pages/demo");

        filter.filter(request);

        assertThat(SessionContext.user()).isEmpty();
        verify(tokenIssuer, never()).parse(anyString());
    }

    @Test
    void publicApiWithBearerStillParsesTokenWhenNotInPermitAll() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setPermitAllPatterns(List.of("/openapi/**"));
        TokenClaims claims = JaxRsFilterTestSupport.validAccessClaims();
        when(tokenIssuer.parse("public-token")).thenReturn(claims);

        ConsoleAuthenticationFilter filter = new ConsoleAuthenticationFilter(security, tokenIssuer);
        ContainerRequestContext request = JaxRsFilterTestSupport.mockRequest(
                "/api/public/pages/demo",
                "GET",
                Map.of(ConsoleHttpHeaders.AUTHORIZATION, "Bearer public-token"));

        filter.filter(request);

        assertThat(SessionContext.user()).isPresent();
        verify(tokenIssuer).parse("public-token");
    }
}
