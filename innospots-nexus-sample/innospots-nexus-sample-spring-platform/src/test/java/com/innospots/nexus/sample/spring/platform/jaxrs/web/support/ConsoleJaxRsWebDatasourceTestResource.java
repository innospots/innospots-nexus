package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;

@Path("/console/datasource")
@Produces(MediaType.APPLICATION_JSON)
public class ConsoleJaxRsWebDatasourceTestResource {

    @GET
    @Path("/demo")
    public R<String> demo() {
        return R.ok("datasource");
    }
}
