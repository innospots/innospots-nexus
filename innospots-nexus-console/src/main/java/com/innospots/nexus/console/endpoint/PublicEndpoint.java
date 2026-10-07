package com.innospots.nexus.console.endpoint;

import jakarta.ws.rs.Path;

import com.innospots.nexus.console.config.ConsoleConstant;

/**
 * 公共开放 REST API 根契约（{@link ConsoleConstant#PUBLIC_API_PREFIX}）。
 *
 * <p>挂载于此前缀下的资源默认纳入 {@link com.innospots.nexus.console.jaxrs.support.ConsolePermitAllPaths}
 *（与 {@code /openapi/**} 相同，免 Bearer 鉴权）。</p>
 */
@Path(ConsoleConstant.PUBLIC_API_PREFIX)
public interface PublicEndpoint {
}
