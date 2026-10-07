package com.innospots.nexus.base.resources;

import java.time.Instant;

/**
 * 已存储资源的不变元数据记录。将资源关联到模块上下文（{@code module} + {@code moduleKey}），
 * 并记录存储详情（URI、存储模式、创建时间）。
 *
 * @author Smars
 * @date 2026/09/13
 * @param resourceId   资源 ID
 * @param resourceName 资源名称
 * @param mimeType     MIME 类型
 * @param module       模块名称
 * @param moduleKey    模块键
 * @param fileSize     文件大小（字节）
 * @param fileUri      文件 URI
 * @param storeMode    存储模式
 * @param createdAt    创建时间
 * @see ResourceStore
 * @see FileResource
 */
public record MetaResource(
        String resourceId,
        String resourceName,
        String mimeType,
        String module,
        String moduleKey,
        long fileSize,
        String fileUri,
        String storeMode,
        Instant createdAt
) {
}
