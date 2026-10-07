package com.innospots.nexus.console.endpoint;

import jakarta.ws.rs.Path;

import com.innospots.nexus.console.config.ConsoleConstant;

/**
 * 公共开放 REST API 根契约（{@link ConsoleConstant#PUBLIC_API_PREFIX}）。
 *
 * <p>挂载于此前缀下的 JAX-RS 资源应在 Web 安全配置中纳入免鉴权路径
 *（默认含 {@code /api/public/**}，见 {@code nexus.console.web.security.permit-all-patterns}）。</p>
 */
@Path(ConsoleConstant.PUBLIC_API_PREFIX)
public interface PublicEndpoint {
}
