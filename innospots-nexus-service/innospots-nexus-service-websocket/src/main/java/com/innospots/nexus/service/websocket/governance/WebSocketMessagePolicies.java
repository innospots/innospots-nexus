package com.innospots.nexus.service.websocket.governance;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;

/**
 * 将 {@link MessageDescriptor} 转为 {@link OperationPolicy}，供调用引擎拦截链消费。
 */
public final class WebSocketMessagePolicies {

    private WebSocketMessagePolicies() {
    }

    /**
     * 根据消息描述符构建调用策略快照（治理键全部生效）。
     *
     * @param descriptor 已注册消息描述符
     * @return 不可变策略
     */
    public static OperationPolicy fromDescriptor(MessageDescriptor descriptor) {
        return fromDescriptor(descriptor, true);
    }

    /**
     * 根据消息描述符构建调用策略快照。
     *
     * @param descriptor            已注册消息描述符
     * @param applyGovernanceKeys   为 {@code false} 时忽略限流/舱壁/熔断/超时键，仅保留权限等字段
     * @return 不可变策略
     */
    public static OperationPolicy fromDescriptor(MessageDescriptor descriptor, boolean applyGovernanceKeys) {
        Checks.notNull(descriptor, "descriptor");
        String rateLimitKey = applyGovernanceKeys ? descriptor.rateLimitKey() : null;
        String bulkheadKey = applyGovernanceKeys ? descriptor.bulkheadKey() : null;
        String circuitKey = applyGovernanceKeys ? descriptor.circuitKey() : null;
        String timeoutPolicyKey = applyGovernanceKeys ? descriptor.timeoutPolicyKey() : null;
        return new OperationPolicy(
                descriptor.permissionKeys(),
                descriptor.resourceResolverKey(),
                rateLimitKey,
                bulkheadKey,
                circuitKey,
                timeoutPolicyKey,
                null,
                false,
                null,
                null,
                null,
                AuditMode.BEST_EFFORT,
                ResponseProfile.LEGACY);
    }

    /**
     * 描述符是否声明了限流/舱壁/熔断/超时任一治理键。
     *
     * @param descriptor 消息描述符
     * @return 是否声明治理键
     */
    public static boolean hasGovernanceKeys(MessageDescriptor descriptor) {
        Checks.notNull(descriptor, "descriptor");
        if (descriptor.rateLimitKey() != null) {
            return true;
        }
        if (descriptor.bulkheadKey() != null) {
            return true;
        }
        if (descriptor.circuitKey() != null) {
            return true;
        }
        return descriptor.timeoutPolicyKey() != null;
    }

    /**
     * 判断是否应经 {@link com.innospots.nexus.service.runtime.invocation.InvocationEngine} 派发。
     *
     * <p>任一治理键或消息权限非空时返回 {@code true}；否则桥接器可直接调用
     * {@link com.innospots.nexus.service.websocket.handler.WebSocketHandler#onMessage}，
     * 避免无策略消息的多余拦截器开销。</p>
     *
     * @param descriptor 消息描述符
     * @return 是否接入引擎
     */
    public static boolean usesInvocationEngine(MessageDescriptor descriptor) {
        Checks.notNull(descriptor, "descriptor");
        if (!descriptor.permissionKeys().isEmpty()) {
            return true;
        }
        return hasGovernanceKeys(descriptor);
    }

    /**
     * 在治理总开关关闭时，是否仍应进入引擎（仅权限等非治理字段）。
     *
     * @param descriptor 消息描述符
     * @return 是否必须经引擎
     */
    public static boolean requiresEngineWhenGovernanceDisabled(MessageDescriptor descriptor) {
        Checks.notNull(descriptor, "descriptor");
        return !descriptor.permissionKeys().isEmpty();
    }
}
