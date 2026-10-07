package com.innospots.nexus.service.http.error;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.http.header.StandardHeaders;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP 错误映射 legacy/problem 形状与已提交响应保护测试。
 */
class HttpErrorMapperTest {

    private final HttpErrorMapper mapper = new HttpErrorMapper();

    @BeforeEach
    void setEnglishLocale() {
        I18nConverter.setLocale(Locale.US);
    }

    @Test
    void legacyProfileMapsRegisteredCodeToR() {
        NexusException failure = NexusException.build(NexusStatusCode.NO_PERMISSION);

        ErrorResponseProfile profile = mapper.map(
                failure,
                ResponseProfile.LEGACY,
                "req-legacy",
                "trace-1",
                null,
                false);

        assertThat(profile.profile()).isEqualTo(ResponseProfile.LEGACY);
        assertThat(profile.httpStatus()).isEqualTo(403);
        assertThat(profile.legacyBody().success()).isFalse();
        assertThat(profile.legacyBody().code()).isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());
        assertThat(profile.legacyBody().message()).contains("No permission");
        assertThat(profile.problemBody()).isNull();
        assertThat(profile.writable()).isTrue();
    }

    @Test
    void problemProfileMapsRegisteredCodeToProblemDetail() {
        NexusException failure = NexusException.build(NexusStatusCode.NO_PERMISSION);

        ErrorResponseProfile profile = mapper.map(
                failure,
                ResponseProfile.PROBLEM,
                "req-problem",
                "trace-2",
                null,
                false);

        assertThat(profile.profile()).isEqualTo(ResponseProfile.PROBLEM);
        assertThat(profile.httpStatus()).isEqualTo(403);
        assertThat(profile.problemBody().type()).hasToString("urn:innospots:problem:AIO040006");
        assertThat(profile.problemBody().instance()).hasToString("urn:request:req-problem");
        assertThat(profile.problemBody().code()).isEqualTo("AIO040006");
        assertThat(profile.problemBody().requestId()).isEqualTo("req-problem");
        assertThat(profile.problemBody().traceId()).isEqualTo("trace-2");
        assertThat(profile.problemBody().status()).isEqualTo(403);
        assertThat(profile.problemBody().title()).contains("No permission");
        assertThat(profile.problemBody().detail()).contains("No permission");
        assertThat(profile.legacyBody()).isNull();
    }

    @Test
    void unknownCodeMapsToSystemError() {
        NexusException failure = NexusException.build("ZZZ019999", "unknown");

        ErrorResponseProfile profile = mapper.map(
                failure,
                ResponseProfile.LEGACY,
                "req-unknown",
                "",
                null,
                false);

        assertThat(profile.httpStatus()).isEqualTo(500);
        assertThat(profile.legacyBody().code()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode());
    }

    @Test
    void committedResponseIsNotRewritten() {
        NexusException failure = NexusException.build(NexusStatusCode.NO_PERMISSION);

        ErrorResponseProfile profile = mapper.map(
                failure,
                ResponseProfile.LEGACY,
                "req-committed",
                "",
                null,
                true);

        assertThat(profile.isCommitted()).isTrue();
        assertThat(profile.writable()).isFalse();
        assertThat(profile.legacyBody()).isNull();
        assertThat(profile.problemBody()).isNull();
    }

    @Test
    void retryAfterHeaderUsesCeilSecondsWithMinimumOne() {
        NexusException failure = NexusException.build(NexusStatusCode.LIMIT_EXCEEDED);

        ErrorResponseProfile profile = mapper.map(
                failure,
                ResponseProfile.LEGACY,
                "req-rate",
                "",
                Duration.ofMillis(500),
                false);

        assertThat(profile.httpStatus()).isEqualTo(429);
        assertThat(profile.headers()).containsEntry(StandardHeaders.RETRY_AFTER, "1");
    }
}
