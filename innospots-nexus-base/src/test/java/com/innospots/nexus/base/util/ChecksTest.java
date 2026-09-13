package com.innospots.nexus.base.util;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecksTest {

    @Test
    void returnsValueWhenNotNull() {
        assertThat(Checks.notNull("nexus", "name")).isEqualTo("nexus");
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> Checks.notNull(null, "userId"))
                .isInstanceOf(NexusException.class)
                .satisfies(error -> {
                    NexusException exception = (NexusException) error;
                    assertThat(exception.code()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
                    assertThat(exception.getMessage()).contains("userId");
                });
    }

    @Test
    void returnsValueWhenNotBlank() {
        assertThat(Checks.notBlank("nexus", "code")).isEqualTo("nexus");
    }

    @Test
    void rejectsBlankValue() {
        assertThatThrownBy(() -> Checks.notBlank("  ", "code"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
    }

    @Test
    void acceptsTrueExpression() {
        Checks.isTrue(true, "must hold");
    }

    @Test
    void rejectsFalseExpression() {
        assertThatThrownBy(() -> Checks.isTrue(false, "pageNo must be positive"))
                .isInstanceOf(NexusException.class)
                .satisfies(error -> {
                    NexusException exception = (NexusException) error;
                    assertThat(exception.code()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
                    assertThat(exception.getMessage()).isEqualTo("pageNo must be positive");
                });
    }

    @Test
    void returnsPositiveValue() {
        assertThat(Checks.positive(3L, "pageNo")).isEqualTo(3L);
    }

    @Test
    void rejectsNonPositiveValue() {
        assertThatThrownBy(() -> Checks.positive(0L, "pageNo"))
                .isInstanceOf(NexusException.class)
                .satisfies(error -> assertThat(((NexusException) error).getMessage()).contains("pageNo"));
    }

    @Test
    void returnsNonEmptyCollection() {
        assertThat(Checks.notEmpty(List.of("a"), "items")).containsExactly("a");
    }

    @Test
    void rejectsEmptyCollection() {
        assertThatThrownBy(() -> Checks.notEmpty(List.of(), "items"))
                .isInstanceOf(NexusException.class)
                .satisfies(error -> assertThat(((NexusException) error).getMessage()).contains("items"));
    }
}
