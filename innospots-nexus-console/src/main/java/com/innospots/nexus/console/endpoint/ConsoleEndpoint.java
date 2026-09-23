package com.innospots.nexus.console.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 根管理控制台端点契约。
 * <p>
 * 本模块仅定义 Jakarta JAX-RS API 边界。运行时绑定、
 * 认证、过滤器与具体实现属于后续
 * 适配器/应用模块。
 * </p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path("/console")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Console", description = "控制台状态与健康")
public interface ConsoleEndpoint {

    /**
     * 供 console 实现暴露的轻量状态端点
     * 平台可用性且不将本模块绑定到 Web 运行时。
     */
    @GET
    @Path("/status")
    @Operation(operationId = "consoleStatus", summary = "控制台可用性探测")
    String status();
}
