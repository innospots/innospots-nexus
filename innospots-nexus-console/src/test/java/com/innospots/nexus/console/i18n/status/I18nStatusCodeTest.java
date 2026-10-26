package com.innospots.nexus.console.i18n.status;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.StatusCategory;

import static org.assertj.core.api.Assertions.assertThat;

class I18nStatusCodeTest {

    @Test
    void definesStableStatusContract() {
        assertThat(Arrays.stream(I18nStatusCode.values())
                .map(I18nStatusCode::fullCode)
                .collect(Collectors.toSet()))
                .hasSize(I18nStatusCode.values().length);

        for (I18nStatusCode status : I18nStatusCode.values()) {
            assertThat(status.module()).isEqualTo("LOC");
            assertThat(status.fullCode()).matches("[A-Z]{3}\\d{2}\\d{4}");
            assertThat(status.fullCode()).isEqualTo(status.module()
                    + status.category().code() + status.localCode());
            assertThat(status.message().value(Locale.US)).isNotBlank();
            assertThat(status.message().value(Locale.SIMPLIFIED_CHINESE)).isNotBlank();
            assertThat(status.advice().value(Locale.US)).isNotBlank();
            assertThat(status.advice().value(Locale.SIMPLIFIED_CHINESE)).isNotBlank();
        }

        assertThat(I18nStatusCode.LOCALE_NOT_SUPPORTED.category())
                .isEqualTo(StatusCategory.INPUT_VALIDATION);
        assertThat(I18nStatusCode.LOCALE_NOT_SUPPORTED.httpStatusCode()).isEqualTo(400);
        assertThat(I18nStatusCode.BUNDLE_NOT_FOUND.category())
                .isEqualTo(StatusCategory.RESOURCE_DATA);
        assertThat(I18nStatusCode.BUNDLE_NOT_FOUND.httpStatusCode()).isEqualTo(404);
        assertThat(I18nStatusCode.BUNDLE_LOAD_FAILED.category())
                .isEqualTo(StatusCategory.INTERNAL_ERROR);
        assertThat(I18nStatusCode.BUNDLE_LOAD_FAILED.httpStatusCode()).isEqualTo(500);
        assertThat(Set.of(I18nStatusCode.values())).hasSize(5);
    }

    @Test
    void nexusExceptionPreservesTypedCode() {
        NexusException exception = NexusException.build(I18nStatusCode.LOCALE_NOT_SUPPORTED);
        assertThat(exception.code()).isEqualTo("LOC010001");
    }
}
