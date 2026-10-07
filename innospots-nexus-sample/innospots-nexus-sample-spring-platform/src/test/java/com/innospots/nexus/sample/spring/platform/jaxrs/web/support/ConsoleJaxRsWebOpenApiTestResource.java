package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;

@Path("/openapi/jaxrs-web-test")
@Produces(MediaType.APPLICATION_JSON)
public class ConsoleJaxRsWebOpenApiTestResource {

    @GET
    @Path("/ping")
    public R<String> ping() {
        return R.ok("ping");
    }
}
