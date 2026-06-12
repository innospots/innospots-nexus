package com.innospots.nexus.console.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Root management-console endpoint contract.
 * <p>
 * This module only defines Jakarta JAX-RS API boundaries. Runtime binding,
 * authentication, filters, and concrete implementations belong in later
 * adapter/application modules.
 * </p>
 */
@Path("/console")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConsoleEndpoint {

    /**
     * Lightweight status endpoint used by console implementations to expose
     * platform availability without binding this module to a web runtime.
     */
    @GET
    @Path("/status")
    String status();
}
