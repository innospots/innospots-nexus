package com.innospots.nexus.spring.service.stream.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.stream.session.StreamSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 {@link StreamSession} 控制器返回值并写 SSE。
 */
public final class ServiceStreamReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final ServletStreamSessionWriter streamWriter;
    private final ServiceContextAccessor contextAccessor;

    /**
     * 创建返回值处理器。
     *
     * @param streamWriter    SSE 写入器
     * @param contextAccessor 上下文访问器
     */
    public ServiceStreamReturnValueHandler(
            ServletStreamSessionWriter streamWriter,
            ServiceContextAccessor contextAccessor) {
        this.streamWriter = Checks.notNull(streamWriter, "streamWriter");
        this.contextAccessor = Checks.notNull(contextAccessor, "contextAccessor");
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return StreamSession.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(
            Object returnValue,
            MethodParameter returnType,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (request == null || response == null) {
            throw new IllegalStateException("servlet request/response unavailable");
        }
        StreamSession<?> session = (StreamSession<?>) returnValue;
        SseEmitter emitter = streamWriter.write(
                request,
                response,
                session,
                contextAccessor.requireCurrent().cancellation());
        DeferredResult<SseEmitter> deferredResult = new DeferredResult<>(emitter.getTimeout());
        WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(deferredResult, mavContainer);
        deferredResult.setResult(emitter);
    }
}
