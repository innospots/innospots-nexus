package com.innospots.nexus.console.scope.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeResolver;
import com.innospots.nexus.console.scope.domain.model.TenantContext;
import com.innospots.nexus.console.scope.domain.model.WorkspaceContext;

/**
 * 基于 console 作用域模型的通用解析器。
 *
 * <p><b>数据来源：</b>租户、工作区与项目都来自客户端 cookie，不来自访问令牌。</p>
 *
 * <p><b>适用范围：</b>只有 {@link SecurityRealm#TENANT}（租户域，即 portal）才有多级作用域；
 * {@link SecurityRealm#PLATFORM} 是运营平台，天然没有租户/工作区概念，直接返回
 * {@code null} 跳过绑定。</p>
 *
 * <p><b>校验链：</b></p>
 * <ol>
 *   <li>安全域必须是 TENANT，否则跳过；</li>
 *   <li>从 cookie 取租户与工作区标识，任一缺失则跳过；</li>
 *   <li>{@link ConsoleTenantContextService} 校验用户在该租户下有有效成员关系；</li>
 *   <li>{@link ConsoleWorkspaceContextService} 校验用户是该工作区成员且工作区属于该租户；</li>
 *   <li>项目若声明，必须属于该工作区。</li>
 * </ol>
 *
 * <p>客户端声明永远不可直接采信：任一步校验失败都返回 {@code null}，
 * 由 {@link com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeSessionFilter}
 * 跳过绑定。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@RequiredArgsConstructor
public class ConsoleWorkspaceScopeResolver implements WorkspaceScopeResolver {

    private final ConsoleTenantContextService tenantContextService;
    private final ConsoleWorkspaceContextService workspaceContextService;
    private final ConsoleProjectLookup projectLookup;
    private final Names names;

    /**
     * 客户端状态 cookie 名。
     *
     * @param tenantId    租户 cookie 名
     * @param workspaceId 工作区 cookie 名
     * @param projectId   项目 cookie 名
     */
    public record Names(String tenantId, String workspaceId, String projectId) {
    }

    /**
     * 项目查询端口：只用于校验声明项目的归属。
     */
    @FunctionalInterface
    public interface ConsoleProjectLookup {

        /**
         * 查找项目快照。
         *
         * @param tenantId    租户标识
         * @param workspaceId 工作区标识
         * @param projectId   项目标识
         * @return 项目快照；不存在时为空
         */
        java.util.Optional<com.innospots.nexus.base.domain.project.ProjectSnapshot> find(
                String tenantId, String workspaceId, String projectId);
    }

    @Override
    public ResolvedWorkspaceScope resolve(String cookieHeader) {
        if (!isTenantRealm()) {
            return null;
        }
        String tenantId = cookieValue(cookieHeader, names.tenantId());
        String workspaceId = cookieValue(cookieHeader, names.workspaceId());
        String projectId = cookieValue(cookieHeader, names.projectId());
        if (isBlank(tenantId) || isBlank(workspaceId)) {
            return null;
        }
        String userId = resolveUserId();
        if (userId == null) {
            return null;
        }
        TenantContext tenantContext = tenantContextService
                .resolve(userId, tenantId.trim())
                .orElse(null);
        if (tenantContext == null) {
            return null;
        }
        WorkspaceContext workspaceContext = workspaceContextService
                .resolve(userId, tenantContext.tenantId(), workspaceId.trim())
                .orElse(null);
        if (workspaceContext == null || !isEnabled(workspaceContext.workspace().getStatus())) {
            return null;
        }
        String resolvedProjectId = null;
        String projectCode = null;
        String projectName = null;
        if (!isBlank(projectId)) {
            var project = projectLookup.find(
                    tenantContext.tenantId(), workspaceContext.workspaceId(), projectId.trim());
            if (project.isEmpty()) {
                return null;
            }
            resolvedProjectId = project.get().projectId();
            projectCode = project.get().projectCode();
            projectName = project.get().projectName();
        }
        return new ResolvedWorkspaceScope(
                tenantContext.tenantId(),
                tenantContext.tenant().getTenantCode(),
                tenantContext.tenant().getTenantName(),
                workspaceContext.workspaceId(),
                workspaceContext.workspace().getWorkspaceCode(),
                workspaceContext.workspace().getWorkspaceName(),
                workspaceContext.workspace().getDescription(),
                resolvedProjectId,
                projectCode,
                projectName);
    }

    /**
     * 取当前用户标识：优先 TLC 用户主键，回退到会话用户名。
     */
    private static String resolveUserId() {
        String userId = TLC.userId();
        if (userId != null && !userId.isBlank()) {
            return userId.trim();
        }
        return null;
    }

    /**
     * 判断当前会话是否为租户域（portal）。
     *
     * <p>未显式设置安全域时按租户域处理：多数部署只有租户域。</p>
     */
    private static boolean isTenantRealm() {
        String realm = TLC.securityRealm();
        return realm == null || realm.isBlank()
                || SecurityRealm.TENANT.name().equalsIgnoreCase(realm);
    }

    /**
     * 从 {@code Cookie} 请求头取出指定 cookie 值。
     *
     * <p>不使用 JAX-RS {@code getCookies()}：该方法依赖 {@code RuntimeDelegate}，
     * 在纯单元测试与部分容器配置下不可用。</p>
     */
    static String cookieValue(String cookieHeader, String name) {
        if (cookieHeader == null || cookieHeader.isBlank() || isBlank(name)) {
            return null;
        }
        for (String pair : cookieHeader.split(";")) {
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            if (!name.equals(pair.substring(0, separator).trim())) {
                continue;
            }
            String value = pair.substring(separator + 1).trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isBlank() ? null : value;
        }
        return null;
    }

    /**
     * 判断工作区状态是否可用于绑定。
     *
     * <p>只接受显式的启用状态：空白或未知状态一律拒绝，避免历史脏数据绕过校验。</p>
     */
    static boolean isEnabled(String status) {
        return BasicStatus.ENABLED.name().equalsIgnoreCase(status)
                || "ACTIVE".equalsIgnoreCase(status);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
