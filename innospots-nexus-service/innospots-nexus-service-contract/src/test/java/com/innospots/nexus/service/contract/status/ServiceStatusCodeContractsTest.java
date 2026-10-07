package com.innospots.nexus.service.contract.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 服务状态码模块唯一性与重试语义契约测试。
 */
class ServiceStatusCodeContractsTest {

    @Test
    void usesSrvModuleAndNineCharacterFullCodes() {
        StatusCode status = ServiceStatusCode.CONTEXT_UNAVAILABLE;

        assertThat(status.module()).isEqualTo("SRV");
        assertThat(status.category()).isEqualTo(StatusCategory.CONFIGURATION);
        assertThat(status.localCode()).isEqualTo("0001");
        assertThat(status.fullCode()).isEqualTo("SRV080001");
        assertThat(status.fullCode()).isEqualTo(status.bisCode());
        assertThat(status.fullCode()).hasSize(9);
        assertThat(status.httpStatusCode()).isEqualTo(500);
    }

    @Test
    void keepsLocalAndFullCodesUnique() {
        assertThat(Arrays.stream(ServiceStatusCode.values()).map(ServiceStatusCode::localCode))
                .allMatch(code -> code.matches("\\d{4}"));
        assertThat(Arrays.stream(ServiceStatusCode.values()).map(ServiceStatusCode::localCode).distinct().count())
                .isEqualTo((long) ServiceStatusCode.values().length);
        assertThat(Arrays.stream(ServiceStatusCode.values()).map(ServiceStatusCode::fullCode).distinct().count())
                .isEqualTo((long) ServiceStatusCode.values().length);
    }

    @Test
    void providesBilingualMessageAndAdviceForEveryCode() {
        assertThat(ServiceStatusCode.values()).allSatisfy(status -> {
            assertThat(status.message().enValue()).isNotBlank();
            assertThat(status.message().cnValue()).isNotBlank();
            assertThat(status.advice().enValue()).isNotBlank();
            assertThat(status.advice().cnValue()).isNotBlank();
            assertThat(status.message().enValue().toLowerCase()).doesNotContain("password", "token", "secret");
            assertThat(status.advice().enValue().toLowerCase()).doesNotContain("password", "token", "secret");
        });
    }

    @Test
    void mapsLockedHttpStatusesAndMessages() {
        assertThat(ServiceStatusCode.DEADLINE_EXCEEDED.fullCode()).isEqualTo("SRV100002");
        assertThat(ServiceStatusCode.DEADLINE_EXCEEDED.httpStatusCode()).isEqualTo(504);
        assertThat(ServiceStatusCode.DEADLINE_EXCEEDED.message().enValue()).isEqualTo("Deadline exceeded");
        assertThat(ServiceStatusCode.DEADLINE_EXCEEDED.message().cnValue()).isEqualTo("执行已超时");
        assertThat(ServiceStatusCode.DEADLINE_EXCEEDED.advice().enValue())
                .isEqualTo("Retry after reducing work or increasing timeout");
        assertThat(ServiceStatusCode.BUFFER_OVERFLOW.httpStatusCode()).isEqualTo(503);
        assertThat(ServiceStatusCode.CAPACITY_EXHAUSTED.httpStatusCode()).isEqualTo(503);
        assertThat(ServiceStatusCode.CIRCUIT_OPEN.httpStatusCode()).isEqualTo(503);
        assertThat(ServiceStatusCode.PAYLOAD_TOO_LARGE.httpStatusCode()).isEqualTo(413);
        assertThat(ServiceStatusCode.MEDIA_TYPE_REJECTED.httpStatusCode()).isEqualTo(415);
        assertThat(ServiceStatusCode.LIFECYCLE_CLOSED.httpStatusCode()).isEqualTo(409);
        assertThat(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN.httpStatusCode()).isEqualTo(400);
        assertThat(ServiceStatusCode.RANGE_NOT_SATISFIABLE.httpStatusCode()).isEqualTo(416);
        assertThat(ServiceStatusCode.UPLOAD_UNSAFE.httpStatusCode()).isEqualTo(422);
        assertThat(ServiceStatusCode.AUDIT_UNAVAILABLE.httpStatusCode()).isEqualTo(503);
        assertThat(ServiceStatusCode.IDEMPOTENCY_CONFLICT.httpStatusCode()).isEqualTo(409);
        assertThat(ServiceStatusCode.OPERATION_CANCELLED.httpStatusCode()).isEqualTo(499);
        assertThat(ServiceStatusCode.CONTENT_READ_FAILED.httpStatusCode()).isEqualTo(502);
        assertThat(ServiceStatusCode.SCANNER_UNAVAILABLE.httpStatusCode()).isEqualTo(503);
        assertThat(ServiceStatusCode.CONTENT_CAPABILITY_MISSING.httpStatusCode()).isEqualTo(501);
        assertThat(ServiceStatusCode.DOWNSTREAM_FAILED.httpStatusCode()).isEqualTo(502);
        assertThat(ServiceStatusCode.PRECONDITION_FAILED.httpStatusCode()).isEqualTo(412);
    }

    @Test
    void preservesCauseWhenBuildingNexusException() {
        IllegalStateException cause = new IllegalStateException("connection reset");
        NexusException exception = NexusException.build(ServiceStatusCode.CONTEXT_UNAVAILABLE, cause);

        assertThat(exception.code()).isEqualTo("SRV080001");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
