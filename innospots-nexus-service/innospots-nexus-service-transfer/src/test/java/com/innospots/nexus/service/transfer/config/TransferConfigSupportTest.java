package com.innospots.nexus.service.transfer.config;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 传输配置启动校验测试。
 */
class TransferConfigSupportTest {

    @Test
    void enabledWithoutReaderFailsWithContentCapabilityMissing() {
        TransferConfig config = new TransferConfig(true, 1024L, com.innospots.nexus.service.transfer.upload.UploadPolicy.defaults(), true);
        assertThatThrownBy(() -> TransferConfigSupport.validateStartup(config, false))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.CONTENT_CAPABILITY_MISSING.fullCode());
    }
}
