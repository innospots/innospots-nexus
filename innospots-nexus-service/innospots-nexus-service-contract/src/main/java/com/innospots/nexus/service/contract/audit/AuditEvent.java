package com.innospots.nexus.service.contract.audit;

import java.time.Instant;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;

/**
 * 服务运行时发出的技术审计事件。
 *
 * @param eventId      用于去重的唯一事件标识
 * @param timestamp    事件时间
 * @param principal    操作主体
 * @param scope        资源作用域
 * @param action       审计动作
 * @param resourceType 资源类型
 * @param resourceId   资源标识，创建前可为空
 * @param requestId    请求标识
 * @param traceId      追踪标识
 * @param result       结果名称
 * @param before       变更前快照
 * @param after        变更后快照
 * @param details      额外安全字段
 * @author Smars
 * @date 2026/09/13
 * @see AuditStorage
 */
public record AuditEvent(
        String eventId,
        Instant timestamp,
        ServicePrincipal principal,
        ServiceScope scope,
        String action,
        String resourceType,
        String resourceId,
        String requestId,
        String traceId,
        String result,
        Map<String, Object> before,
        Map<String, Object> after,
        Map<String, Object> details
) {

    public AuditEvent {
        Checks.notBlank(eventId, "eventId");
        Checks.notNull(timestamp, "timestamp");
        Checks.notNull(principal, "principal");
        Checks.notNull(scope, "scope");
        Checks.notBlank(action, "action");
        Checks.notBlank(resourceType, "resourceType");
        Checks.notBlank(requestId, "requestId");
        Checks.notNull(traceId, "traceId");
        Checks.notBlank(result, "result");
        resourceId = resourceId == null || resourceId.isBlank() ? null : resourceId;
        before = before == null ? Map.of() : Map.copyOf(before);
        after = after == null ? Map.of() : Map.copyOf(after);
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
