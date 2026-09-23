package com.innospots.nexus.service.transfer.upload;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 上传资源策略校验器。
 */
public final class UploadValidator {

    private final UploadPolicy policy;

    /**
     * 创建校验器。
     *
     * @param policy 上传策略
     */
    public UploadValidator(UploadPolicy policy) {
        this.policy = Checks.notNull(policy, "policy");
    }

    /**
     * 校验单个上传资源。
     *
     * @param resource 上传资源
     */
    public void validate(UploadResource resource) {
        Checks.notNull(resource, "resource");
        if (policy.maxFileBytes() > 0 && resource.size() > policy.maxFileBytes()) {
            throw NexusException.build(ServiceStatusCode.PAYLOAD_TOO_LARGE);
        }
        if (!policy.allowedContentTypes().isEmpty()) {
            String contentType = resource.contentType();
            if (contentType == null || !policy.allowedContentTypes().contains(contentType)) {
                throw NexusException.build(ServiceStatusCode.MEDIA_TYPE_REJECTED);
            }
        }
    }
}
