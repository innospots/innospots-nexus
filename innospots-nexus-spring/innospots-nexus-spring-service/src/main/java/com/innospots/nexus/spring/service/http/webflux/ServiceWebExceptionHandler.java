package com.innospots.nexus.spring.service.http.webflux;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.http.error.ErrorResponseProfile;
import com.innospots.nexus.service.http.error.HttpErrorMapper;
import com.innospots.nexus.service.http.error.ProblemDetailVo;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.spring.service.core.ServiceProperties;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycle;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycleAccessor;

import reactor.core.publisher.Mono;

/**
 * 将 {@link NexusException} 映射为 HTTP 错误响应。
 */
@Order(-2)
public final class ServiceWebExceptionHandler implements WebExceptionHandler {

    private final HttpErrorMapper errorMapper;
    private final ServiceProperties properties;
    private final ServiceRequestLifecycleAccessor lifecycleAccessor;

    /**
     * 创建 WebFlux 异常处理器。
     */
    public ServiceWebExceptionHandler(
            HttpErrorMapper errorMapper,
            ServiceProperties properties,
            ServiceRequestLifecycleAccessor lifecycleAccessor) {
        this.errorMapper = Checks.notNull(errorMapper, "errorMapper");
        this.properties = Checks.notNull(properties, "properties");
        this.lifecycleAccessor = Checks.notNull(lifecycleAccessor, "lifecycleAccessor");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (!(exception instanceof NexusException nexusException)) {
            return Mono.error(exception);
        }
        String requestId = resolveRequestId(exchange);
        Duration retryAfter = retryAfter(nexusException);
        ErrorResponseProfile profile = errorMapper.map(
                nexusException,
                properties.getResponseProfile(),
                requestId,
                "",
                retryAfter,
                false);
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(profile.httpStatus()));
        exchange.getResponse().getHeaders().add(StandardHeaders.REQUEST_ID, requestId);
        profile.headers().forEach((name, value) -> exchange.getResponse().getHeaders().add(name, value));
        if (!profile.writable()) {
            return exchange.getResponse().setComplete();
        }
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = serializeBody(profile);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.fromSupplier(() -> exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE);
        if (value instanceof ServiceRequestLifecycle lifecycle) {
            return lifecycle.requestId();
        }
        try {
            return lifecycleAccessor.requireCurrent().requestId();
        } catch (NexusException ex) {
            return "";
        }
    }

    private static String serializeBody(ErrorResponseProfile profile) {
        if (profile.profile() == ResponseProfile.PROBLEM) {
            ProblemDetailVo body = profile.problemBody();
            return Jsons.toJson(body);
        }
        R<Void> body = profile.legacyBody();
        return Jsons.toJson(body);
    }

    private static Duration retryAfter(NexusException exception) {
        if (NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(exception.code())) {
            return Duration.ofSeconds(1);
        }
        return null;
    }
}
