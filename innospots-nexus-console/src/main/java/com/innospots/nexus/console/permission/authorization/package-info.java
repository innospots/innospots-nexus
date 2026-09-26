/**
 * 控制台页面权限判定契约（catalog PAGE/DATASOURCE）及主体解析；与 Bearer 身份认证无关。
 *
 * <p>HTTP 适配层见 spring-console 的 {@code ConsolePagePermissionFilter}；
 * 核心判定见 {@link ConsolePagePermissionAuthorizer}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.console.permission.authorization;
