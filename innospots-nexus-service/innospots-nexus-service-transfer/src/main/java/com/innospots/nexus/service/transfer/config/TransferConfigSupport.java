package com.innospots.nexus.service.transfer.config;

/**
 * 文件传输配置。
 */
public final class TransferConfigSupport {

    private TransferConfigSupport() {
    }

    /**
     * 校验文件能力是否可用。
     *
     * @param config 传输配置
     * @param readerPresent 是否提供 {@link com.innospots.nexus.service.transfer.content.ResourceContentReader}
     */
    public static void validateStartup(TransferConfig config, boolean readerPresent) {
        if (config.fileEnabled() && config.startupValidationEnabled() && !readerPresent) {
            throw com.innospots.nexus.base.exception.NexusException.build(
                    com.innospots.nexus.service.contract.status.ServiceStatusCode.CONTENT_CAPABILITY_MISSING);
        }
    }
}
