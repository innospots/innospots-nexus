package com.innospots.nexus.service.contract.channel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OverflowPolicy} 稳定枚举契约。
 */
class OverflowPolicyContractsTest {

    @Test
    void exposesStableValues() {
        assertThat(OverflowPolicy.values())
                .containsExactly(
                        OverflowPolicy.REJECT,
                        OverflowPolicy.DROP_LATEST,
                        OverflowPolicy.DROP_OLDEST,
                        OverflowPolicy.CLOSE);
    }
}
