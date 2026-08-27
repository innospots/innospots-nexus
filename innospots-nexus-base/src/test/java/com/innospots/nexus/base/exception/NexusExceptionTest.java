package com.innospots.nexus.base.exception;

import com.innospots.nexus.base.status.NexusStatusCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NexusExceptionTest {

    @Test
    void exceptionCarriesCodeAndMessage() {
        NexusException exception = NexusException.build("CONFIG_MISSING", "Required config is missing");

        assertThat(exception.code()).isEqualTo("CONFIG_MISSING");
        assertThat(exception.getMessage()).isEqualTo("Required config is missing");
    }

    @Test
    void buildsExceptionFromStatusCode() {
        NexusException exception = NexusException.build(NexusStatusCode.CONFIG_ERROR);

        assertThat(exception.code()).isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
        assertThat(exception.getMessage()).contains("配置错误");
    }

    @Test
    void buildsExceptionFromStatusCodeWithOverrideMessage() {
        NexusException exception = NexusException.build(NexusStatusCode.INVALID_PARAMETER, "userId must not be blank");

        assertThat(exception.code()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
        assertThat(exception.getMessage()).isEqualTo("userId must not be blank");
    }

    @Test
    void buildsExceptionFromStatusCodeWithCause() {
        IllegalStateException cause = new IllegalStateException("boom");
        NexusException exception = NexusException.build(NexusStatusCode.SERIALIZATION_FAILED, cause);

        assertThat(exception.code()).isEqualTo(NexusStatusCode.SERIALIZATION_FAILED.fullCode());
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
