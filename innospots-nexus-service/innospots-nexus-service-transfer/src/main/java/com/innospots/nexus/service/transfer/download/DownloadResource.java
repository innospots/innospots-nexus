package com.innospots.nexus.service.transfer.download;

import java.time.Instant;
import java.util.function.Supplier;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.BinarySource;
import com.innospots.nexus.service.transfer.content.ByteRange;
import com.innospots.nexus.service.transfer.content.ContentMetadata;

/**
 * 下载响应描述。
 *
 * @param resourceId  资源标识
 * @param filename    展示文件名
 * @param metadata    内容元数据
 * @param content     延迟内容打开入口
 */
public record DownloadResource(
        String resourceId,
        String filename,
        ContentMetadata metadata,
        Supplier<java.util.concurrent.CompletionStage<BinarySource>> content
) {

    public DownloadResource {
        Checks.notBlank(resourceId, "resourceId");
        filename = blankToNull(filename);
        Checks.notNull(metadata, "metadata");
        Checks.notNull(content, "content");
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
