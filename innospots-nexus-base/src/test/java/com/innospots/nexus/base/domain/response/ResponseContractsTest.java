package com.innospots.nexus.base.domain.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ResponseContractsTest {

    @Test
    void createsApiResponseAndPageResponse() {
        R<PageResult<String>> response = R.ok(PageResult.of(List.of("a", "b"), 2, 10, 23));

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("OK");
        assertThat(response.data().pages()).isEqualTo(3);
        assertThat(response.data().hasNext()).isTrue();
        assertThat(response.data().hasPrevious()).isTrue();
    }

    @Test
    void rejectsInvalidPageArguments() {
        assertThatIllegalArgumentException().isThrownBy(() -> PageResult.empty(0, 10));
        assertThatIllegalArgumentException().isThrownBy(() -> PageResult.empty(1, 0));
    }
}
