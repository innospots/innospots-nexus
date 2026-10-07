package com.innospots.nexus.service.transfer.content;

import com.innospots.nexus.base.util.Checks;

/**
 * 字节范围，闭区间端点。
 *
 * @param startInclusive 起始字节（含）
 * @param endInclusive   结束字节（含）
 */
public record ByteRange(long startInclusive, long endInclusive) {

    public ByteRange {
        Checks.isTrue(startInclusive >= 0, "startInclusive must not be negative");
        Checks.isTrue(endInclusive >= startInclusive, "endInclusive must be >= startInclusive");
    }

    /**
     * 返回范围长度。
     *
     * @return 字节数
     */
    public long length() {
        return endInclusive - startInclusive + 1;
    }
}
