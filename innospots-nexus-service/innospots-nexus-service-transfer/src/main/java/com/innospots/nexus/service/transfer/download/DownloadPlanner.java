package com.innospots.nexus.service.transfer.download;

import com.innospots.nexus.service.transfer.content.ContentMetadata;

/**
 * 条件请求与 Range 规划器。
 */
public interface DownloadPlanner {

    /**
     * 根据请求与元数据生成下载计划。
     *
     * @param request  下载请求
     * @param metadata 内容元数据
     * @return 下载计划
     */
    DownloadPlan plan(DownloadRequest request, ContentMetadata metadata);
}
