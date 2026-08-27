package com.innospots.nexus.base.domain.response;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
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

    @Test
    void failsFromStatusCode() {
        R<Void> response = R.fail(NexusStatusCode.DATA_NOT_FOUND);

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo(NexusStatusCode.DATA_NOT_FOUND.fullCode());
        assertThat(response.message()).contains("数据不存在");
        assertThat(response.data()).isNull();
    }

    @Test
    void failsFromStatusCodeWithPayload() {
        R<String> response = R.fail(NexusStatusCode.BUSINESS_ERROR, "detail");

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo(NexusStatusCode.BUSINESS_ERROR.fullCode());
        assertThat(response.data()).isEqualTo("detail");
    }

    @Test
    void mapsExceptionToFailureResponse() {
        NexusException exception = NexusException.build(NexusStatusCode.NO_PERMISSION, "missing grant");
        R<Void> response = R.from(exception);

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo(exception.code());
        assertThat(response.message()).isEqualTo("missing grant");
        assertThat(response.display()).isEqualTo(exception.display());
    }
}
