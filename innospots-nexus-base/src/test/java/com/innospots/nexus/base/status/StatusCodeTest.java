package com.innospots.nexus.base.status;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusCodeTest {

    @Test
    void buildsFullCodeFromModuleCategoryAndLocalCode() {
        StatusCode statusCode = NexusStatusCode.CONFIG_ERROR;

        assertThat(statusCode.module()).isEqualTo("NEX");
        assertThat(statusCode.category()).isEqualTo(StatusCategory.CONFIGURATION);
        assertThat(statusCode.localCode()).isEqualTo("0002");
        assertThat(statusCode.bisCode()).isEqualTo("NEX080002");
        assertThat(statusCode.fullCode()).isEqualTo("NEX080002");
        assertThat(statusCode.httpStatusCode()).isEqualTo(500);
        assertThat(statusCode.message().enValue()).isEqualTo("Configuration error");
        assertThat(statusCode.message().cnValue()).isEqualTo("配置错误");
        assertThat(statusCode.advice().enValue()).isEqualTo("Please check the configuration");
        assertThat(statusCode.advice().cnValue()).isEqualTo("请检查配置");
        assertThat(statusCode.summary()).contains("配置错误");
    }

    @Test
    void validatesStatusCodeParts() {
        assertThatThrownBy(() -> StatusCodeRules.requireValid("NX", StatusCategory.GENERAL, "001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StatusCodeRules.requireValid("NEX", StatusCategory.GENERAL, "001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findsStatusCodeByFullCode() {
        Optional<NexusStatusCode> statusCode = NexusStatusCode.findByFullCode("NEX010001");

        assertThat(statusCode).contains(NexusStatusCode.INVALID_PARAMETER);
    }

    @Test
    void keepsLocalCodesFourDigitsAndGloballyUnique() {
        assertThat(Arrays.stream(NexusStatusCode.values()).map(NexusStatusCode::localCode))
                .allMatch(code -> code.matches("\\d{4}"));
        assertThat(Arrays.stream(NexusStatusCode.values()).map(NexusStatusCode::localCode).distinct().count())
                .isEqualTo(NexusStatusCode.values().length);
    }

    @Test
    void definesPlatformLevelStatusCodesWithoutJsonSplit() {
        assertThat(NexusStatusCode.values()).contains(
                NexusStatusCode.SUCCESS,
                NexusStatusCode.INVALID_PARAMETER,
                NexusStatusCode.CONFIG_ERROR,
                NexusStatusCode.SERIALIZATION_FAILED,
                NexusStatusCode.DATA_NOT_FOUND,
                NexusStatusCode.RESOURCE_NOT_FOUND,
                NexusStatusCode.NO_PERMISSION,
                NexusStatusCode.AUTHENTICATION_FAILED,
                NexusStatusCode.PASSWORD_ERROR,
                NexusStatusCode.USER_NOT_FOUND,
                NexusStatusCode.EXECUTION_FAILED,
                NexusStatusCode.BUSINESS_ERROR,
                NexusStatusCode.LIMIT_EXCEEDED,
                NexusStatusCode.RETRY_FAILED,
                NexusStatusCode.OPTIMISTIC_LOCK_FAILED,
                NexusStatusCode.COMPILE_FAILED,
                NexusStatusCode.INITIALIZATION_FAILED,
                NexusStatusCode.SYSTEM_ERROR
        );
        assertThat(Arrays.stream(NexusStatusCode.values()).map(Enum::name))
                .noneMatch(name -> name.startsWith("JSON_"));
        assertThat(NexusStatusCode.SERIALIZATION_FAILED.message().cnValue()).isEqualTo("序列化失败");
    }
}
