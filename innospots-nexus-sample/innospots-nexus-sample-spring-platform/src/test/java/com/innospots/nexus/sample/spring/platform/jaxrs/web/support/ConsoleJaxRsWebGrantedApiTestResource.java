package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

/**
 * 命中默认 {@code /api/d/**} 权限 Filter，且带 {@link EndpointGrant} 的测试资源。
 */
@Path("/api/d/platform/jaxrs-web-test")
@Produces(MediaType.APPLICATION_JSON)
@EndpointModule("jaxrs-web-test")
public class ConsoleJaxRsWebGrantedApiTestResource {

    @GET
    @Path("/secured-echo")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "jaxrs.web.test.echo",
            label = @I18nLiteral(pairs = {"zh-CN", "JaxRs Web 测试回声", "en", "JaxRs web test echo"}))
    public R<String> securedEcho() {
        return R.ok("granted-api");
    }
}
