package com.innospots.nexus.spring.service.transfer.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.download.DownloadResource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理 {@link DownloadResource} 控制器返回值。
 */
public final class ServiceDownloadReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final ServletDownloadWriter downloadWriter;

    /**
     * 创建返回值处理器。
     *
     * @param downloadWriter Servlet 下载写入器
     */
    public ServiceDownloadReturnValueHandler(ServletDownloadWriter downloadWriter) {
        this.downloadWriter = Checks.notNull(downloadWriter, "downloadWriter");
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return DownloadResource.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(
            Object returnValue,
            MethodParameter returnType,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) {
        mavContainer.setRequestHandled(true);
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (request == null || response == null) {
            throw new IllegalStateException("servlet request/response unavailable");
        }
        downloadWriter.write(
                request.getMethod(),
                com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport.servletHeaders(request),
                response,
                (DownloadResource) returnValue);
    }
}
