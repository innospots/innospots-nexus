package com.innospots.nexus.core.endpoint.contract;

import jakarta.ws.rs.Path;

/**
 * 公共开放 REST API 根契约（{@link ConsoleRestPaths#PUBLIC_API_PREFIX}）。
 *
 *（与 {@code /openapi/**} 相同，免 Bearer 鉴权）。</p>
 */
@Path(ConsoleRestPaths.PUBLIC_API_PREFIX)
public interface PublicEndpoint {
}
