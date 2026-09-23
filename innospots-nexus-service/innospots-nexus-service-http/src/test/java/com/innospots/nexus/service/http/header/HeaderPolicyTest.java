package com.innospots.nexus.service.http.header;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HTTP 头策略校验测试。
 */
class HeaderPolicyTest {

    private final HeaderPolicy policy = new HeaderPolicy();

    @Test
    void rejectsDuplicateInconsistentAuthorizationValues() {
        Map<String, List<String>> headers = Map.of(
                StandardHeaders.AUTHORIZATION, List.of("Bearer one", "Bearer two"));

        assertThatThrownBy(() -> policy.validate(headers))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
    }

    @Test
    void allowsDuplicateIdenticalAuthorizationValues() {
        Map<String, List<String>> headers = Map.of(
                StandardHeaders.AUTHORIZATION, List.of("Bearer same", "Bearer same"));

        policy.validate(headers);
    }
}
