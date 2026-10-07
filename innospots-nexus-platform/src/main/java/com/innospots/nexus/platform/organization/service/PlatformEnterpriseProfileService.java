package com.innospots.nexus.platform.organization.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;
import com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode;
import com.innospots.nexus.platform.organization.domain.request.EnterpriseProfileUpsertRequest;
import com.innospots.nexus.platform.organization.domain.vo.EnterpriseProfileVo;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;

/**
 * 租户下企业法定档案（{@code nx_pl_enterprise}）的查询与 upsert 编排。
 * <p>与 {@link PlatformTenantService} 解耦：开通租户不强制创建企业行。
 * 仅 {@link TenantType#ENTERPRISE} 形态允许维护法定档案。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint
 * @see com.innospots.nexus.platform.organization.operator.EnterpriseOperator
 */
@RequiredArgsConstructor
public class PlatformEnterpriseProfileService {

    private final TenantOperator tenantOperator;
    private final EnterpriseOperator enterpriseOperator;

    /**
     * 查询租户的企业法定档案。
     *
     * @param tenantId 租户标识
     * @return 企业档案概要
     */
    public EnterpriseProfileVo getEnterpriseProfile(String tenantId) {
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        requireEnterpriseTenantType(tenant);
        return toVo(enterpriseOperator.requireByTenantId(tenantId));
    }

    /**
     * 创建或更新企业法定档案（按租户一对一）。
     *
     * @param tenantId 租户标识
     * @param request  法定名称必填，其余字段可选
     * @return 持久化后的企业档案概要
     */
    @Transactional
    public EnterpriseProfileVo upsertEnterpriseProfile(String tenantId, EnterpriseProfileUpsertRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.legalName(), "legalName");
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        requireEnterpriseTenantType(tenant);
        return enterpriseOperator.findByTenantId(tenantId)
                .map(existing -> updateExisting(existing, request))
                .orElseGet(() -> createNew(tenantId, request));
    }

    private EnterpriseProfileVo createNew(String tenantId, EnterpriseProfileUpsertRequest request) {
        EnterpriseEntity enterprise = new EnterpriseEntity();
        enterprise.setTenantId(tenantId);
        applyProfileFields(enterprise, request);
        enterpriseOperator.insert(enterprise);
        return toVo(enterprise);
    }

    private EnterpriseProfileVo updateExisting(EnterpriseEntity existing, EnterpriseProfileUpsertRequest request) {
        applyProfileFields(existing, request);
        enterpriseOperator.update(existing);
        return toVo(existing);
    }

    private static void applyProfileFields(EnterpriseEntity enterprise, EnterpriseProfileUpsertRequest request) {
        enterprise.setLegalName(request.legalName().trim());
        enterprise.setCreditCode(request.creditCode());
        enterprise.setIndustry(request.industry());
        enterprise.setContactName(request.contactName());
        enterprise.setContactPhone(request.contactPhone());
        enterprise.setContactEmail(request.contactEmail());
        enterprise.setAddress(request.address());
        enterprise.setExtra(request.extra());
    }

    private static void requireEnterpriseTenantType(TenantEntity tenant) {
        if (!TenantType.ENTERPRISE.name().equals(tenant.getTenantType())) {
            throw NexusException.build(PlatformOrganizationStatusCode.ENTERPRISE_PROFILE_TENANT_TYPE_MISMATCH);
        }
    }

    private static EnterpriseProfileVo toVo(EnterpriseEntity enterprise) {
        return new EnterpriseProfileVo(
                enterprise.getEnterpriseId(),
                enterprise.getTenantId(),
                enterprise.getLegalName(),
                enterprise.getCreditCode(),
                enterprise.getIndustry(),
                enterprise.getContactName(),
                enterprise.getContactPhone(),
                enterprise.getContactEmail(),
                enterprise.getAddress(),
                enterprise.getExtra());
    }
}
