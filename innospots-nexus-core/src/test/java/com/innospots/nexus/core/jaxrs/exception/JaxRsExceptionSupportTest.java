package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;

import static org.assertj.core.api.Assertions.assertThat;

class JaxRsExceptionSupportTest {

    private final JaxRsExceptionSupport support = new JaxRsExceptionSupport();

    @Test
    void mapsNexusExceptionToLegacyRWithHttpStatusAndRequestIdHeader() {
        NexusException exception = NexusException.build(NexusStatusCode.INVALID_PARAMETER);

        Response response = support.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
        assertThat(response.getHeaderString(HttpHeaderNames.REQUEST_ID)).isEqualTo("unknown");
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void keepsNativeWebApplicationResponseWithoutLegacyRWrapping() {
        WebApplicationException exception = new NotFoundException();

        Response response = support.toWebApplicationResponse(exception);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isNull();
    }

    @Test
    void mapsUnknownThrowableToSystemError() {
        NexusException mapped = NexusException.build(NexusStatusCode.SYSTEM_ERROR, new IllegalStateException("boom"));

        Response response = support.toResponse(mapped);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void mapsProductDomainStatusCodeCarriedByException() {
        // 产品域状态码（非 AIO* 平台码）按自身 httpStatusCode 返回，而非兜底 500
        NexusException exception = NexusException.build(TestStatusCode.NOT_FOUND);

        Response response = support.toResponse(exception);

        assertThat(exception.statusCode()).isEqualTo(TestStatusCode.NOT_FOUND);
        assertThat(exception.code()).isEqualTo("TSK130001");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void resolvesRawErrorCodeWithoutStatusCodeToSystemError() {
        // 仅携带原始错误码的异常无状态码，未命中平台码时仍为 500 兜底
        NexusException exception = NexusException.build("TSK139999", "unknown code");

        Response response = support.toResponse(exception);

        assertThat(exception.statusCode()).isNull();
        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
    }

    /** 产品域测试状态码（模块 TSK），验证异常携带状态码后的 HTTP 解析。 */
    private enum TestStatusCode implements StatusCode {

        NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, 404);

        private final String localCode;
        private final StatusCategory category;
        private final int httpStatusCode;

        TestStatusCode(String localCode, StatusCategory category, int httpStatusCode) {
            this.localCode = localCode;
            this.category = category;
            this.httpStatusCode = httpStatusCode;
        }

        @Override
        public String module() {
            return "TSK";
        }

        @Override
        public StatusCategory category() {
            return category;
        }

        @Override
        public String localCode() {
            return localCode;
        }

        @Override
        public I18nObject message() {
            return I18nObject.of("en", "Task not found", "zh", "任务不存在");
        }

        @Override
        public I18nObject advice() {
            return I18nObject.of("en", "Check the task key", "zh", "请检查任务标识");
        }

        @Override
        public int httpStatusCode() {
            return httpStatusCode;
        }
    }
}
