package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;

@Path("/console/jaxrs-web-test")
@Produces(MediaType.APPLICATION_JSON)
public class ConsoleJaxRsWebSecuredTestResource {

    @GET
    @Path("/secured-echo")
    public R<String> securedEcho() {
        return R.ok("secured");
    }

    @GET
    @Path("/session-user-id")
    public R<String> sessionUserId() {
        String userId = SessionContext.user()
                .map(user -> String.valueOf(user.userId()))
                .orElse("none");
        return R.ok(userId);
    }

    @GET
    @Path("/nexus-error")
    public R<Void> nexusError() {
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, "bad parameter");
    }

    @GET
    @Path("/runtime-error")
    public R<Void> runtimeError() {
        throw new IllegalStateException("simulated failure");
    }
}
