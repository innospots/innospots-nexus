package com.innospots.nexus.service.http.header;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * requestId 解析与生成策略测试。
 */
class RequestIdPolicyTest {

    private final RequestIdPolicy policy = new RequestIdPolicy();

    @Test
    void acceptsValidIncomingRequestId() {
        String resolved = policy.resolve(Optional.of("req-123.test_ok"));

        assertThat(resolved).isEqualTo("req-123.test_ok");
    }

    @Test
    void rejectsInvalidIncomingRequestId() {
        assertThat(policy.isValid("bad id")).isFalse();
        assertThat(policy.isValid("")).isFalse();

        String resolved = policy.resolve(Optional.of("bad id"));

        assertThat(resolved).isNotBlank();
        assertThat(policy.isValid(resolved)).isTrue();
    }

    @Test
    void generatesValidRequestIdWhenMissing() {
        String generated = policy.generate();

        assertThat(policy.isValid(generated)).isTrue();
    }
}
