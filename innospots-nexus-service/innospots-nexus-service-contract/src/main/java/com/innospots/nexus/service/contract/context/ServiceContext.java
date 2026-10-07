package com.innospots.nexus.service.contract.context;

import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

/**
 * 每次调用的不可变服务上下文。各组件均为必填；可选能力缺失时使用空快照或无限截止时间，而非 null。
 *
 * @param requestId    唯一请求标识
 * @param request      传输元数据
 * @param security     已认证或匿名主体
 * @param scope        租户/工作区/项目作用域
 * @param trace        追踪快照，可能未采样
 * @param cancellation 取消令牌
 * @param deadline     剩余执行截止时间
 * @param attributes   类型化属性
 * @author Smars
 * @date 2026/09/13
 * @see ServiceContextAccessor
 * @see ServicePrincipal
 */
public record ServiceContext(
        String requestId,
        RequestMetadata request,
        ServicePrincipal security,
        ServiceScope scope,
        TraceSnapshot trace,
        CancellationToken cancellation,
        Deadline deadline,
        ContextAttributes attributes
) {

    public ServiceContext {
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(request, "request");
        Checks.notNull(security, "security");
        Checks.notNull(scope, "scope");
        Checks.notNull(trace, "trace");
        Checks.notNull(cancellation, "cancellation");
        Checks.notNull(deadline, "deadline");
        Checks.notNull(attributes, "attributes");
    }

    /**
     * 返回安全主体组件。
     *
     * @return 主体
     */
    public ServicePrincipal principal() {
        return security;
    }

    /**
     * 从请求元数据返回可选客户端标识。
     *
     * @return 存在时的客户端标识
     */
    public Optional<String> clientId() {
        return Optional.ofNullable(request.clientId());
    }

    /**
     * 返回可选租户标识。
     *
     * @return 存在时的租户标识
     */
    public Optional<String> tenantId() {
        return Optional.ofNullable(scope.tenantId());
    }

    /**
     * 返回可选工作区标识。
     *
     * @return 存在时的工作区标识
     */
    public Optional<String> workspaceId() {
        return Optional.ofNullable(scope.workspaceId());
    }

    /**
     * 返回可选项目标识。
     *
     * @return 存在时的项目标识
     */
    public Optional<String> projectId() {
        return Optional.ofNullable(scope.projectId());
    }
}
