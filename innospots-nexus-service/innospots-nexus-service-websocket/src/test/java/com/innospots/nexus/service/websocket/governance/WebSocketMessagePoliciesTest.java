package com.innospots.nexus.service.websocket.governance;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.websocket.message.FrameType;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebSocketMessagePolicies} 物化与判定测试。
 */
class WebSocketMessagePoliciesTest {

    @Test
    void fromDescriptorStripsGovernanceKeysWhenDisabled() {
        MessageDescriptor descriptor = new MessageDescriptor(
                "t",
                String.class,
                String.class,
                Set.of("ws.read"),
                null,
                ExecutionMode.BLOCKING,
                FrameType.TEXT,
                "rate",
                "bulk",
                "circuit",
                "timeout");
        OperationPolicy policy = WebSocketMessagePolicies.fromDescriptor(descriptor, false);
        assertThat(policy.permissionKeys()).containsExactly("ws.read");
        assertThat(policy.rateLimitKey()).isNull();
        assertThat(policy.bulkheadKey()).isNull();
        assertThat(policy.circuitKey()).isNull();
        assertThat(policy.timeoutPolicyKey()).isNull();
    }

    @Test
    void requiresEngineWhenGovernanceDisabledOnlyForPermission() {
        MessageDescriptor permissionOnly = descriptor(Set.of("p"), null);
        MessageDescriptor rateOnly = descriptor(Set.of(), "rate");
        assertThat(WebSocketMessagePolicies.requiresEngineWhenGovernanceDisabled(permissionOnly)).isTrue();
        assertThat(WebSocketMessagePolicies.requiresEngineWhenGovernanceDisabled(rateOnly)).isFalse();
    }

    private static MessageDescriptor descriptor(Set<String> permissions, String rateKey) {
        return new MessageDescriptor(
                "t",
                String.class,
                String.class,
                permissions,
                null,
                ExecutionMode.BLOCKING,
                FrameType.TEXT,
                rateKey,
                null,
                null,
                null);
    }
}
