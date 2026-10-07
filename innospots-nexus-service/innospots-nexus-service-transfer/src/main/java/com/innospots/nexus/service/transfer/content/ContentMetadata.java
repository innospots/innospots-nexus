package com.innospots.nexus.service.transfer.content;

import java.time.Instant;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * 资源内容元数据。
 *
 * @param resourceId      资源标识
 * @param size            总字节数
 * @param contentType     内容类型
 * @param etag            实体标签
 * @param lastModified    最后修改时间
 * @param checksums       已知校验和
 * @param rangeSupported  是否支持随机范围读取
 */
public record ContentMetadata(
        String resourceId,
        long size,
        String contentType,
        String etag,
        Instant lastModified,
        Map<String, String> checksums,
        boolean rangeSupported
) {

    public ContentMetadata {
        Checks.notBlank(resourceId, "resourceId");
        Checks.isTrue(size >= 0, "size must not be negative");
        contentType = blankToNull(contentType);
        etag = blankToNull(etag);
        checksums = checksums == null ? Map.of() : Map.copyOf(checksums);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
