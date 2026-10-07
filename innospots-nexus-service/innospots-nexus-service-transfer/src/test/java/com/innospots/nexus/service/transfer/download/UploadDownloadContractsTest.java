package com.innospots.nexus.service.transfer.download;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.transfer.content.ByteRange;
import com.innospots.nexus.service.transfer.content.ContentMetadata;
import com.innospots.nexus.service.transfer.content.PublisherBinarySource;
import com.innospots.nexus.service.transfer.upload.ScanResult;
import com.innospots.nexus.service.transfer.upload.UploadPolicy;
import com.innospots.nexus.service.transfer.upload.UploadResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 上传/下载 record 形状契约测试。
 */
class UploadDownloadContractsTest {

    @Test
    void uploadResourceExposesFieldShape() {
        UploadResource resource = new UploadResource(
                "file",
                "demo.txt",
                "text/plain",
                12L,
                PublisherBinarySource.ofBytes("hello".getBytes()));
        assertThat(resource.fieldName()).isEqualTo("file");
        assertThat(resource.filename()).isEqualTo("demo.txt");
        assertThat(resource.contentType()).isEqualTo("text/plain");
        assertThat(resource.size()).isEqualTo(12L);
        assertThat(resource.content()).isNotNull();
    }

    @Test
    void downloadPlanExposesFieldShape() {
        DownloadPlan plan = new DownloadPlan(
                206,
                100L,
                "bytes 0-99/1000",
                java.util.Map.of("Content-Type", "application/octet-stream"),
                new ByteRange(0, 99),
                true);
        assertThat(plan.httpStatus()).isEqualTo(206);
        assertThat(plan.contentLength()).isEqualTo(100L);
        assertThat(plan.contentRange()).contains("bytes");
        assertThat(plan.headers()).containsEntry("Content-Type", "application/octet-stream");
        assertThat(plan.readRange()).isEqualTo(new ByteRange(0, 99));
        assertThat(plan.bodyRequired()).isTrue();
    }

    @Test
    void scanResultContainsAllOutcomes() {
        assertThat(ScanResult.values()).containsExactly(ScanResult.CLEAN, ScanResult.INFECTED, ScanResult.UNAVAILABLE);
    }

    @Test
    void uploadPolicyDefaultsMatchRuntimeBaseline() {
        UploadPolicy policy = UploadPolicy.defaults();
        assertThat(policy.maxFiles()).isEqualTo(10);
        assertThat(policy.maxFileBytes()).isEqualTo(100L * 1024L * 1024L);
        assertThat(policy.maxTotalBytes()).isEqualTo(200L * 1024L * 1024L);
    }

    @Test
    void defaultPlannerReturnsNotModifiedWhenEtagMatches() {
        ContentMetadata metadata = new ContentMetadata(
                "res-1",
                1000L,
                "text/plain",
                "\"v1\"",
                Instant.parse("2026-09-15T00:00:00Z"),
                java.util.Map.of(),
                true);
        DownloadRequest request = new DownloadRequest("GET", null, "\"v1\"", null, null, null, null);
        DownloadPlan plan = new DefaultDownloadPlanner().plan(request, metadata);
        assertThat(plan.httpStatus()).isEqualTo(304);
        assertThat(plan.bodyRequired()).isFalse();
    }
}
