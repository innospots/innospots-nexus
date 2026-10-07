package com.innospots.nexus.service.transfer.config;

import java.time.Duration;

import com.innospots.nexus.service.transfer.upload.UploadPolicy;

/**
 * 文件传输模块配置。
 *
 * @param fileEnabled               是否启用文件传输
 * @param smallObjectThresholdBytes 小对象兼容阈值
 * @param uploadPolicy              上传策略
 * @param startupValidationEnabled  启动时校验 Reader 能力
 */
public record TransferConfig(
        boolean fileEnabled,
        long smallObjectThresholdBytes,
        UploadPolicy uploadPolicy,
        boolean startupValidationEnabled
) {

    /**
     * 返回默认配置（文件传输默认关闭）。
     *
     * @return 默认配置
     */
    public static TransferConfig defaults() {
        return new TransferConfig(false, 1024L * 1024L, UploadPolicy.defaults(), false);
    }

    /**
     * 返回上传读取超时默认值。
     *
     * @return 超时
     */
    public static Duration defaultUploadReadTimeout() {
        return Duration.ofMinutes(5);
    }
}
