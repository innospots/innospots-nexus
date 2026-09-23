package com.innospots.nexus.service.contract.policy;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * 单次调用的<strong>已解析操作策略</strong>快照，由注解或目录在边界处物化后注入
 * {@link com.innospots.nexus.service.contract.invocation.InvocationContext}。
 *
 * <p><strong>用途</strong>：把安全、治理、审计、错误体等横切关注点从业务方法签名中剥离，
 * 供 {@code InvocationEngine} 各拦截器只读消费。各 {@code *Key} 字段为<strong>配置表键名</strong>；
 * {@code null} 或空白表示该能力未启用。</p>
 *
 * <p><strong>来源</strong>：通常由
 * {@link com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver} 从
 * {@link com.innospots.nexus.service.contract.policy.annotation} 与
 * {@link com.innospots.nexus.service.contract.security.annotation} 解析；
 * 亦可通过 {@link PolicyCatalog} 静态注册（adapter 启动期）。</p>
 *
 * <p><strong>治理字段解析顺序（超时）</strong>：拦截器优先 {@link #timeout()} 固定时长，
 * 其次 {@link #timeoutPolicyKey()} 查 {@code GovernanceConfig.timeouts}，
 * 最后回退 {@code InvocationContext.operationId()} 查同表。</p>
 *
 * <p><strong>不变式</strong>：权限集不可变；{@code timeout} 若存在须为正；{@code auditMode}、
 * {@code responseProfile} 缺省为 BEST_EFFORT / LEGACY。</p>
 *
 * @param permissionKeys      合并后的所需权限键（类型级在前、方法级在后去重）；{@code PublicAccess} 时为空
 * @param resourceResolverKey 权限注解中的资源解析器键，可选
 * @param rateLimitKey        限流策略键，映射 {@code rateLimits}；未配置限流时为 {@code null}
 * @param bulkheadKey         舱壁策略键，映射 {@code bulkheads}
 * @param circuitKey          断路器策略键，映射 {@code circuits}
 * @param timeoutPolicyKey    {@link com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected} 键，映射 {@code timeouts}
 * @param timeout             可选固定正数超时时长，优先于 {@code timeoutPolicyKey}
 * @param audited             是否记录审计意图
 * @param auditAction         审计动作名
 * @param auditResourceType   审计资源类型
 * @param auditSnapshotKey    审计快照提供者键
 * @param auditMode           REQUIRED 或 BEST_EFFORT
 * @param responseProfile     HTTP 错误体形态
 * @author Smars
 * @date 2026/09/13
 * @see OperationDescriptor
 * @see PolicyCatalog
 * @see com.innospots.nexus.service.contract.invocation.InvocationContext
 */
public record OperationPolicy(
        /** 合并后的权限键；{@code PublicAccess} 解析结果为空集。 */
        Set<String> permissionKeys,
        /** {@link com.innospots.nexus.service.contract.security.annotation.RequiresPermission#resource()} 解析键。 */
        String resourceResolverKey,
        /** {@link com.innospots.nexus.service.contract.policy.annotation.RateLimited} 配置键。 */
        String rateLimitKey,
        /** {@link com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected} 配置键。 */
        String bulkheadKey,
        /** {@link com.innospots.nexus.service.contract.policy.annotation.CircuitProtected} 配置键。 */
        String circuitKey,
        /** {@link com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected} 配置键，查 {@code timeouts} 表。 */
        String timeoutPolicyKey,
        /** 固定正数超时时长；优先于 {@link #timeoutPolicyKey()} 与 operationId 查表。 */
        Duration timeout,
        /** 是否启用审计拦截器意图。 */
        boolean audited,
        /** 审计动作标识。 */
        String auditAction,
        /** 审计资源类型。 */
        String auditResourceType,
        /** 审计快照提供者键。 */
        String auditSnapshotKey,
        /** REQUIRED 须在事务内落审计；BEST_EFFORT 异步尽力。 */
        AuditMode auditMode,
        /** HTTP 错误响应体形态（如 LEGACY）。 */
        ResponseProfile responseProfile
) {

    /**
     * 规范化策略字段并校验超时为正。
     */
    public OperationPolicy {
        if (permissionKeys == null || permissionKeys.isEmpty()) {
            permissionKeys = Set.of();
        } else {
            permissionKeys = Collections.unmodifiableSet(new LinkedHashSet<>(permissionKeys));
        }
        resourceResolverKey = blankToNull(resourceResolverKey);
        rateLimitKey = blankToNull(rateLimitKey);
        bulkheadKey = blankToNull(bulkheadKey);
        circuitKey = blankToNull(circuitKey);
        timeoutPolicyKey = blankToNull(timeoutPolicyKey);
        auditAction = blankToNull(auditAction);
        auditResourceType = blankToNull(auditResourceType);
        auditSnapshotKey = blankToNull(auditSnapshotKey);
        if (timeout != null) {
            Checks.isTrue(!timeout.isZero() && !timeout.isNegative(), "timeout must be positive");
        }
        auditMode = auditMode == null ? AuditMode.BEST_EFFORT : auditMode;
        responseProfile = responseProfile == null ? ResponseProfile.LEGACY : responseProfile;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
