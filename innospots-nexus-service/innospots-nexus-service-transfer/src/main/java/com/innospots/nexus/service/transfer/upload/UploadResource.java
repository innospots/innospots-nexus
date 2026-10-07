package com.innospots.nexus.service.transfer.upload;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.BinarySource;

/**
 * 单次上传资源描述。
 *
 * @param fieldName    表单字段名
 * @param filename     原始文件名
 * @param contentType  内容类型
 * @param size         声明大小，-1 表示未知
 * @param content      内容源
 */
public record UploadResource(
        String fieldName,
        String filename,
        String contentType,
        long size,
        BinarySource content
) {

    public UploadResource {
        Checks.notBlank(fieldName, "fieldName");
        Checks.notBlank(filename, "filename");
        contentType = blankToNull(contentType);
        Checks.isTrue(size == -1 || size >= 0, "size must be -1 or non-negative");
        Checks.notNull(content, "content");
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
