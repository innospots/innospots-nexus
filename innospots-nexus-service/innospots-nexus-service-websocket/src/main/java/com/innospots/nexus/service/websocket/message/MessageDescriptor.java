package com.innospots.nexus.service.websocket.message;

import java.lang.reflect.Type;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;

/**
 * 已注册 WebSocket 消息类型描述。
 *
 * <p>治理相关字段为 {@link com.innospots.nexus.service.contract.policy.OperationPolicy} 中同名
 * 配置表键；{@code null} 表示该消息类型不启用对应能力。入站派发时由
 * {@link com.innospots.nexus.service.websocket.governance.WebSocketGovernedMessageDispatcher}
 * 物化策略并接入 {@link com.innospots.nexus.service.runtime.invocation.InvocationEngine}。</p>
 *
 * @param type                 消息类型
 * @param inputType            入站载荷类型
 * @param outputType           出站载荷类型
 * @param permissionKeys       消息权限键
 * @param resourceResolverKey  资源解析器键（<strong>保留未实现</strong>，当前固定 {@code websocket.message} 资源）
 * @param executionMode        执行模式
 * @param frameType            帧编码类型
 * @param rateLimitKey         限流策略键，映射 {@code GovernanceConfig.rateLimits}
 * @param bulkheadKey          舱壁策略键，映射 {@code GovernanceConfig.bulkheads}
 * @param circuitKey           熔断策略键，映射 {@code GovernanceConfig.circuits}
 * @param timeoutPolicyKey     超时策略键，映射 {@code GovernanceConfig.timeouts}
 * @author Smars
 * @date 2026/09/15
 */
public record MessageDescriptor(
        String type,
        Type inputType,
        Type outputType,
        Set<String> permissionKeys,
        String resourceResolverKey,
        ExecutionMode executionMode,
        FrameType frameType,
        /** 限流配置表键；映射 {@code GovernanceConfig.rateLimits}，{@code null} 表示该消息不限流。 */
        String rateLimitKey,
        /** 舱壁配置表键；映射 {@code GovernanceConfig.bulkheads}，{@code null} 表示不限制并发。 */
        String bulkheadKey,
        /** 熔断配置表键；映射 {@code GovernanceConfig.circuits}，通常用于整段 {@code onMessage} 保护。 */
        String circuitKey,
        /** 超时配置表键；映射 {@code GovernanceConfig.timeouts}，在 {@code invokeAsync} 全程生效。 */
        String timeoutPolicyKey
) {

    public MessageDescriptor {
        Checks.notBlank(type, "type");
        Checks.notNull(inputType, "inputType");
        Checks.notNull(outputType, "outputType");
        permissionKeys = permissionKeys == null ? Set.of() : Set.copyOf(permissionKeys);
        executionMode = executionMode == null ? ExecutionMode.BLOCKING : executionMode;
        frameType = frameType == null ? FrameType.TEXT : frameType;
        rateLimitKey = blankToNull(rateLimitKey);
        bulkheadKey = blankToNull(bulkheadKey);
        circuitKey = blankToNull(circuitKey);
        timeoutPolicyKey = blankToNull(timeoutPolicyKey);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
