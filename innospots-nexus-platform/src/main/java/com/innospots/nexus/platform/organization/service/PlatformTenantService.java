package com.innospots.nexus.platform.organization.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.organization.domain.event.TenantCreatedEvent;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantPageRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantStatusUpdateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantUpdateRequest;
import com.innospots.nexus.platform.organization.domain.vo.TenantVo;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;
import com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity;

/**
 * 平台租户开通、分页查询与生命周期编排。
 * <p>不在此写入 {@code nx_pl_enterprise}；查询视图可附带已存在的企业摘要字段。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint
 * @see PlatformEnterpriseProfileService
 * @see com.innospots.nexus.platform.organization.operator.TenantOperator
 */
@RequiredArgsConstructor
public class PlatformTenantService {

    private final TenantOperator tenantOperator;
    private final EnterpriseOperator enterpriseOperator;

    /**
     * 开通租户（仅 {@code nx_pl_tenant}）。
     *
     * @param request 租户身份字段
     * @return 创建后的租户概要（含可选企业摘要）
     */
    @Transactional
    public TenantVo createTenant(TenantCreateRequest request) {
        Objects.requireNonNull(request, "request");
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantName(request.tenantName());
        tenant.setTenantCode(request.tenantCode());
        tenant.setTenantType(request.tenantType());
        tenant.setPlanCode(request.planCode());
        tenant.setOwnerTenantUserId(request.ownerTenantUserId());
        tenantOperator.insert(tenant);
        EventBus.publish(new TenantCreatedEvent(
                tenant.getTenantId(),
                tenant.getTenantCode(),
                tenant.getOwnerTenantUserId()));
        return toVo(tenant);
    }

    /**
     * 按条件分页查询租户。
     *
     * @param request 分页与筛选条件；{@code null} 时使用默认分页
     * @return 租户概要分页
     */
    public PageResult<TenantVo> pageTenants(TenantPageRequest request) {
        PageResult<TenantEntity> page = tenantOperator.page(request);
        return PageResult.of(
                page.records().stream().map(this::toVo).toList(),
                page.pageNo(),
                page.pageSize(),
                page.total());
    }

    /**
     * 按 ID 查询单个租户。
     *
     * @param tenantId 租户标识
     * @return 租户概要
     */
    public TenantVo getTenant(String tenantId) {
        return toVo(tenantOperator.requireById(tenantId));
    }

    /**
     * 更新租户显示名称、套餐与 owner 等可编辑字段。
     *
     * @param tenantId 租户标识
     * @param request  非空字段参与更新
     * @return 更新后的租户概要
     */
    @Transactional
    public TenantVo updateTenant(String tenantId, TenantUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        if (request.tenantName() != null && !request.tenantName().isBlank()) {
            tenant.setTenantName(request.tenantName().trim());
        }
        if (request.planCode() != null) {
            tenant.setPlanCode(request.planCode());
        }
        if (request.ownerTenantUserId() != null) {
            tenant.setOwnerTenantUserId(request.ownerTenantUserId());
        }
        tenantOperator.update(tenant);
        return toVo(tenant);
    }

    /**
     * 更新租户生命周期状态。
     *
     * @param tenantId 租户标识
     * @param request  目标状态（{@link com.innospots.nexus.platform.organization.domain.enums.TenantStatus} 名称）
     * @return 更新后的租户概要
     */
    @Transactional
    public TenantVo updateTenantStatus(String tenantId, TenantStatusUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        TenantOperator.requireTenantStatus(request.status());
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        tenant.setStatus(request.status().trim().toUpperCase());
        tenantOperator.update(tenant);
        return toVo(tenant);
    }

    private TenantVo toVo(TenantEntity tenant) {
        EnterpriseEntity enterprise = enterpriseOperator.findByTenantId(tenant.getTenantId()).orElse(null);
        String enterpriseId = enterprise == null ? null : enterprise.getEnterpriseId();
        String legalName = enterprise == null ? null : enterprise.getLegalName();
        return new TenantVo(
                tenant.getTenantId(),
                tenant.getTenantName(),
                tenant.getTenantCode(),
                tenant.getTenantType(),
                tenant.getStatus(),
                tenant.getPlanCode(),
                tenant.getOwnerTenantUserId(),
                enterpriseId,
                legalName);
    }
}
