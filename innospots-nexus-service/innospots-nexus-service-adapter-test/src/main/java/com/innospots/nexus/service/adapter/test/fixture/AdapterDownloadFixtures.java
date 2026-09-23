package com.innospots.nexus.service.adapter.test.fixture;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.innospots.nexus.service.transfer.content.ContentMetadata;
import com.innospots.nexus.service.transfer.content.PublisherBinarySource;
import com.innospots.nexus.service.transfer.download.DownloadResource;

/**
 * adapter 下载夹具工厂。
 */
public final class AdapterDownloadFixtures {

    public static final byte[] DOWNLOAD_BYTES = "download-ok".getBytes(StandardCharsets.UTF_8);

    private AdapterDownloadFixtures() {
    }

    /**
     * 返回内存下载资源。
     *
     * @return 下载资源
     */
    public static DownloadResource memoryDownload() {
        ContentMetadata metadata = new ContentMetadata(
                "adapter-download",
                DOWNLOAD_BYTES.length,
                "text/plain",
                "\"v1\"",
                Instant.parse("2026-09-15T00:00:00Z"),
                Map.of(),
                false);
        return new DownloadResource(
                "adapter-download",
                "download.txt",
                metadata,
                () -> CompletableFuture.completedFuture(PublisherBinarySource.ofBytes(DOWNLOAD_BYTES)));
    }
}
