package com.innospots.nexus.console.support;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** {@link ConsoleExceptions} 构建与日志行为测试。 */
class ConsoleExceptionsTest {

    @Test
    void buildsSameExceptionAsNexusExceptionFactory() {
        NexusException expected = NexusException.build(NexusStatusCode.INVALID_PARAMETER);

        NexusException actual = ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);

        assertThat(actual.code()).isEqualTo(expected.code());
        assertThat(actual.getMessage()).isEqualTo(expected.getMessage());
    }

    @Test
    void propagatesBuiltExceptionWhenThrown() {
        assertThatThrownBy(() -> {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }).isInstanceOfSatisfying(NexusException.class, exception -> assertThat(exception.code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode()));
    }
}
