package com.innospots.nexus.quarkus.service.rest;

import java.time.Duration;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.quarkus.service.config.ServiceConfig;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.http.error.ErrorResponseProfile;
import com.innospots.nexus.service.http.error.HttpErrorMapper;
import com.innospots.nexus.service.http.error.ProblemDetailVo;
import com.innospots.nexus.service.http.header.StandardHeaders;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * 将 {@link NexusException} 映射为 HTTP 错误响应。
 */
@Provider
@ApplicationScoped
public final class ServiceExceptionMapper implements ExceptionMapper<NexusException> {

    private final HttpErrorMapper errorMapper;
    private final ServiceConfig serviceConfig;
    private final ServiceRequestLifecycleAccessor lifecycleAccessor;

    public ServiceExceptionMapper(
            HttpErrorMapper errorMapper,
            ServiceConfig serviceConfig,
            ServiceRequestLifecycleAccessor lifecycleAccessor) {
        this.errorMapper = Checks.notNull(errorMapper, "errorMapper");
        this.serviceConfig = Checks.notNull(serviceConfig, "serviceConfig");
        this.lifecycleAccessor = Checks.notNull(lifecycleAccessor, "lifecycleAccessor");
    }

    @Override
    public Response toResponse(NexusException exception) {
        String requestId = lifecycleAccessor.requireCurrent().requestId();
        Duration retryAfter = retryAfter(exception);
        ErrorResponseProfile profile = errorMapper.map(
                exception,
                serviceConfig.responseProfile(),
                requestId,
                "",
                retryAfter,
                false);
        if (!profile.writable()) {
            return Response.status(profile.httpStatus()).build();
        }
        Response.ResponseBuilder builder = Response.status(profile.httpStatus())
                .header(StandardHeaders.REQUEST_ID, requestId);
        profile.headers().forEach(builder::header);
        if (profile.profile() == ResponseProfile.PROBLEM) {
            ProblemDetailVo body = profile.problemBody();
            return builder.entity(body).build();
        }
        R<Void> body = profile.legacyBody();
        return builder.entity(body).build();
    }

    private static Duration retryAfter(NexusException exception) {
        if (NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(exception.code())) {
            return Duration.ofSeconds(1);
        }
        return null;
    }
}
