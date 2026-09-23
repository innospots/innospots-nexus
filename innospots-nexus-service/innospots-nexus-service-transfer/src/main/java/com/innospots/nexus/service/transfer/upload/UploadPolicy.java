package com.innospots.nexus.service.transfer.upload;

import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * 上传限制策略。
 *
 * @param maxFileBytes         单文件最大字节，-1 表示不限制
 * @param maxFiles             单请求最大文件数
 * @param maxTotalBytes        单请求总字节上限
 * @param allowedContentTypes  允许的内容类型，空集合表示不限制
 */
public record UploadPolicy(
        long maxFileBytes,
        int maxFiles,
        long maxTotalBytes,
        Set<String> allowedContentTypes
) {

    public UploadPolicy {
        Checks.isTrue(maxFileBytes == -1 || maxFileBytes > 0, "maxFileBytes must be -1 or positive");
        Checks.isTrue(maxFiles > 0, "maxFiles must be positive");
        Checks.isTrue(maxTotalBytes > 0, "maxTotalBytes must be positive");
        allowedContentTypes = allowedContentTypes == null ? Set.of() : Set.copyOf(allowedContentTypes);
    }

    /**
     * 返回默认上传策略。
     *
     * @return 默认策略
     */
    public static UploadPolicy defaults() {
        return new UploadPolicy(
                100L * 1024L * 1024L,
                10,
                200L * 1024L * 1024L,
                Set.of());
    }
}
