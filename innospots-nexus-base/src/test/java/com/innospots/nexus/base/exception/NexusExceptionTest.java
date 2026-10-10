package com.innospots.nexus.base.exception;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link NexusException} 状态码携带语义：使用 {@code StatusCode} 构建时
 * 保留原始状态码，使用原始错误码构建时不携带。
 */
class NexusExceptionTest {

    @Test
    void retainsStatusCodeAcrossStatusCodeFactories() {
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION).statusCode())
                .isEqualTo(NexusStatusCode.NO_PERMISSION);
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        NexusStatusCode.NO_PERMISSION.message()).statusCode())
                .isEqualTo(NexusStatusCode.NO_PERMISSION);
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        NexusStatusCode.NO_PERMISSION.message(),
                        new IllegalStateException("boom")).statusCode())
                .isEqualTo(NexusStatusCode.NO_PERMISSION);
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        "覆盖消息").statusCode())
                .isEqualTo(NexusStatusCode.NO_PERMISSION);
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        new IllegalStateException("boom")).statusCode())
                .isEqualTo(NexusStatusCode.NO_PERMISSION);
    }

    @Test
    void overrideMessageFallsBackToSummaryWhenBlank() {
        String summary = NexusStatusCode.NO_PERMISSION.summary();

        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        "覆盖消息").getMessage())
                .isEqualTo("覆盖消息");
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION,
                        " ").getMessage())
                .isEqualTo(summary);
        assertThat(NexusException.build(NexusStatusCode.NO_PERMISSION)
                        .getMessage())
                .isEqualTo(summary);
    }

    @Test
    void rawCodeFactoriesCarryNoStatusCode() {
        assertThat(NexusException.build("TSK139999", "custom").statusCode())
                .isNull();
        assertThat(NexusException.build("TSK139999", "custom",
                        new IllegalStateException("boom")).statusCode())
                .isNull();
    }
}
