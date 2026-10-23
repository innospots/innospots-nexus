package com.innospots.nexus.core.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class JaxRsExceptionSupportTest {

    @Test
    void mapsNexusExceptionToLegacyRWithHttpStatusAndRequestIdHeader() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
            assertThat(requestId).isEqualTo("unknown");
            assertThat(body.code()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
            return stubResponse(httpStatus, requestId, body);
        });

        NexusException exception = NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        Response response = support.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.INVALID_PARAMETER.httpStatusCode());
        assertThat(response.getHeaderString(HttpHeaderNames.REQUEST_ID)).isEqualTo("unknown");
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void keepsNativeWebApplicationResponseWithoutLegacyRWrapping() {
        Response nativeResponse = mock(Response.class);
        Response.StatusType statusType = mock(Response.StatusType.class);
        when(statusType.getStatusCode()).thenReturn(404);
        when(nativeResponse.getStatus()).thenReturn(404);
        when(nativeResponse.getStatusInfo()).thenReturn(statusType);
        when(nativeResponse.getEntity()).thenReturn(null);
        WebApplicationException exception = new WebApplicationException(nativeResponse);

        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            throw new AssertionError("should not build legacy R for native WebApplicationException");
        });

        Response response = support.toWebApplicationResponse(exception);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isNull();
    }

    @Test
    void mapsUnknownThrowableToSystemError() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
            return stubResponse(httpStatus, requestId, body);
        });

        NexusException mapped = NexusException.build(NexusStatusCode.SYSTEM_ERROR, new IllegalStateException("boom"));
        Response response = support.toResponse(mapped);

        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void mapsProductDomainStatusCodeCarriedByException() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(404);
            return stubResponse(httpStatus, requestId, body);
        });

        NexusException exception = NexusException.build(TestStatusCode.NOT_FOUND);
        Response response = support.toResponse(exception);

        assertThat(exception.statusCode()).isEqualTo(TestStatusCode.NOT_FOUND);
        assertThat(exception.code()).isEqualTo("TSK130001");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity()).isInstanceOf(R.class);
    }

    @Test
    void resolvesRawErrorCodeWithoutStatusCodeToSystemError() {
        JaxRsExceptionSupport support = new JaxRsExceptionSupport((httpStatus, body, requestId) -> {
            assertThat(httpStatus).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
            return stubResponse(httpStatus, requestId, body);
        });

        NexusException exception = NexusException.build("TSK139999", "unknown code");
        Response response = support.toResponse(exception);

        assertThat(exception.statusCode()).isNull();
        assertThat(response.getStatus()).isEqualTo(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
    }

    private static Response stubResponse(int httpStatus, String requestId, R<Void> body) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(httpStatus);
        when(response.getHeaderString(HttpHeaderNames.REQUEST_ID)).thenReturn(requestId);
        when(response.getEntity()).thenReturn(body);
        return response;
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
