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
}
