/**
 * 控制台资源权限判定契约及主体解析；与 Bearer 身份认证无关。
 *
 * <p>HTTP 适配层见 {@code ConsoleResourcePermissionFilter}：按 {@code ResolvedEndpointGrant}
 * 拼资源键后判定；核心见 {@link ConsoleResourcePermissionAuthorizer}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.console.permission.authorization;
