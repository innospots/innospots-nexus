package com.innospots.nexus.spring.service.http.mvc;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.http.error.ErrorResponseProfile;
import com.innospots.nexus.service.http.error.HttpErrorMapper;
import com.innospots.nexus.service.http.error.ProblemDetailVo;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.spring.service.core.ServiceProperties;

/**
 * 将 {@link NexusException} 映射为 HTTP 错误响应。
 */
@RestControllerAdvice
public final class ServiceExceptionAdvice {

    private final HttpErrorMapper errorMapper;
    private final ServiceProperties properties;
    private final ServiceRequestLifecycleAccessor lifecycleAccessor;

    /**
     * 创建异常 advice。
     */
    public ServiceExceptionAdvice(
            HttpErrorMapper errorMapper,
            ServiceProperties properties,
            ServiceRequestLifecycleAccessor lifecycleAccessor) {
        this.errorMapper = Checks.notNull(errorMapper, "errorMapper");
        this.properties = Checks.notNull(properties, "properties");
        this.lifecycleAccessor = Checks.notNull(lifecycleAccessor, "lifecycleAccessor");
    }

    /**
     * 处理平台异常。
     *
     * @param exception 平台异常
     * @return HTTP 响应
     */
    @ExceptionHandler(NexusException.class)
    public ResponseEntity<?> handleNexusException(NexusException exception) {
        String requestId = lifecycleAccessor.requireCurrent().requestId();
        Duration retryAfter = retryAfter(exception);
        ErrorResponseProfile profile = errorMapper.map(
                exception,
                properties.getResponseProfile(),
                requestId,
                "",
                retryAfter,
                false);
        if (!profile.writable()) {
            return ResponseEntity.status(profile.httpStatus()).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(StandardHeaders.REQUEST_ID, requestId);
        profile.headers().forEach(headers::add);
        if (profile.profile() == ResponseProfile.PROBLEM) {
            ProblemDetailVo body = profile.problemBody();
            return ResponseEntity.status(profile.httpStatus()).headers(headers).body(body);
        }
        R<Void> body = profile.legacyBody();
        return ResponseEntity.status(profile.httpStatus()).headers(headers).body(body);
    }

    private static Duration retryAfter(NexusException exception) {
        if (NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(exception.code())) {
            return Duration.ofSeconds(1);
        }
        return null;
    }
}
