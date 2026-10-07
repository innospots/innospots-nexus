package com.innospots.nexus.spring.service.transfer.mvc;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.BinarySources;
import com.innospots.nexus.service.transfer.download.DownloadPlan;
import com.innospots.nexus.service.transfer.download.DownloadRequests;
import com.innospots.nexus.service.transfer.download.DownloadResource;
import com.innospots.nexus.service.transfer.download.DownloadTransferSupport;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 将 {@link DownloadResource} 写入 Servlet 响应。
 */
public final class ServletDownloadWriter {

    private final DownloadTransferSupport transferSupport;

    /**
     * 创建写入器。
     *
     * @param transferSupport 下载执行器
     */
    public ServletDownloadWriter(DownloadTransferSupport transferSupport) {
        this.transferSupport = Checks.notNull(transferSupport, "transferSupport");
    }

    /**
     * 写回下载响应。
     *
     * @param method   HTTP 方法
     * @param headers  小写化请求头
     * @param response Servlet 响应
     * @param resource 下载资源
     */
    public void write(
            String method,
            Map<String, List<String>> headers,
            HttpServletResponse response,
            DownloadResource resource) {
        Checks.notNull(response, "response");
        Checks.notNull(resource, "resource");
        DownloadTransferSupport.PreparedDownload prepared = transferSupport.prepare(
                DownloadRequests.from(method, headers),
                resource);
        applyPlan(response, resource, prepared.plan());
        if (prepared.body() == null) {
            return;
        }
        try {
            BinarySources.writeTo(prepared.body(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception ex) {
            throw new IllegalStateException("download write failed", ex);
        }
    }

    private static void applyPlan(HttpServletResponse response, DownloadResource resource, DownloadPlan plan) {
        response.setStatus(plan.httpStatus());
        plan.headers().forEach(response::setHeader);
        if (resource.filename() != null && !resource.filename().isBlank()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.filename() + "\"");
        }
    }
}
