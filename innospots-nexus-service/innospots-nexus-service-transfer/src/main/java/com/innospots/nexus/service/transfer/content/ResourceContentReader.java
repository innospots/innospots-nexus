package com.innospots.nexus.service.transfer.content;

import java.util.concurrent.CompletionStage;

/**
 * 宿主绑定的流式内容读取 SPI。
 */
public interface ResourceContentReader {

    /**
     * 读取元数据。
     *
     * @param resourceId 资源标识
     * @return 元数据阶段
     */
    CompletionStage<ContentMetadata> metadata(String resourceId);

    /**
     * 打开内容流。
     *
     * @param resourceId 资源标识
     * @param range      可选范围，{@code null} 表示全量
     * @return 二进制源阶段
     */
    CompletionStage<BinarySource> open(String resourceId, ByteRange range);
}
