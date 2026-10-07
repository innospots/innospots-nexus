package com.innospots.nexus.service.stream.session;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 流会话状态契约测试。
 */
class StreamSessionContractsTest {

    @Test
    void streamStateContainsLifecycleValues() {
        assertThat(StreamState.values())
                .containsExactly(
                        StreamState.CREATED,
                        StreamState.OPEN,
                        StreamState.COMPLETED,
                        StreamState.FAILED,
                        StreamState.CANCELLED);
    }
}
