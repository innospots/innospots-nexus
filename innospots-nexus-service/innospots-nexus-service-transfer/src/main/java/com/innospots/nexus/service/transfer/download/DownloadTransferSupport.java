package com.innospots.nexus.service.transfer.download;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.BinarySource;
import com.innospots.nexus.service.transfer.content.BinarySources;
import com.innospots.nexus.service.transfer.content.ByteRange;

/**
 * 下载计划执行器。仅通过 {@link DownloadResource#content()} 打开内存/流式内容。
 */
public final class DownloadTransferSupport {

    private final DownloadPlanner planner;

    /**
     * 创建执行器。
     *
     * @param planner 下载规划器
     */
    public DownloadTransferSupport(DownloadPlanner planner) {
        this.planner = Checks.notNull(planner, "planner");
    }

    /**
     * 生成下载计划并打开 body。
     *
     * @param request  下载请求
     * @param resource 下载资源
     * @return 准备好的下载
     */
    public PreparedDownload prepare(DownloadRequest request, DownloadResource resource) {
        Checks.notNull(request, "request");
        Checks.notNull(resource, "resource");
        DownloadPlan plan = planner.plan(request, resource.metadata());
        BinarySource body = null;
        if (plan.bodyRequired()) {
            BinarySource opened = resource.content().get().toCompletableFuture().join();
            ByteRange readRange = plan.readRange();
            if (readRange != null && resource.metadata().rangeSupported()) {
                body = BinarySources.viewRange(opened, readRange);
            } else {
                body = opened;
            }
        }
        return new PreparedDownload(plan, body);
    }

    /**
     * 准备好的下载响应。
     *
     * @param plan 下载计划
     * @param body 可选 body 源
     */
    public record PreparedDownload(DownloadPlan plan, BinarySource body) {
    }
}
