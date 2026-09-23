package com.innospots.nexus.platform.support.operator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.platform.support.dao.SupportAccessGrantDao;
import com.innospots.nexus.platform.support.domain.entity.SupportAccessGrantEntity;
import com.innospots.nexus.platform.support.domain.enums.SupportAccessStatus;
import com.innospots.nexus.platform.support.domain.request.SupportAccessGrantCreateRequest;

/**
 * 将平台支持访问授权持久化到租户。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class SupportAccessGrantOperator {

    private final SupportAccessGrantDao supportAccessGrantDao;

    /**
     * 创建待审批授权。审批前不激活租户访问。
     *
     * @param request 授权创建数据
     * @return persisted 授权
     */
    @Transactional
    public SupportAccessGrantEntity create(SupportAccessGrantCreateRequest request) {
        requireText(request == null ? null : request.tenantId(), "tenantId");
        requireText(request == null ? null : request.platformUserId(), "platformUserId");
        requireText(request == null ? null : request.reason(), "reason");
        if (request.expireAt() == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(), "expireAt is required");
        }

        SupportAccessGrantEntity grant = new SupportAccessGrantEntity();
        grant.setTenantId(request.tenantId());
        grant.setPlatformUserId(request.platformUserId());
        grant.setReason(request.reason());
        grant.setExpireAt(request.expireAt());
        grant.setStatus(SupportAccessStatus.PENDING.name());
        supportAccessGrantDao.insert(grant);
        log.info("Created support access grant {} for tenant {}", grant.getGrantId(), grant.getTenantId());
        return grant;
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }
}
