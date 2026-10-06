/**
 * 运营管理平台组织域：平台侧租户身份（{@code nx_pl_tenant}）与企业法定档案（{@code nx_pl_enterprise}）。
 *
 * <p>租户开通与企业档案维护解耦：{@code POST /api/platform/tenants} 仅创建租户记录；
 * 企业信息经 {@code PUT /api/platform/tenants/{tenantId}/enterprise} 单独维护，且仅
 * {@link com.innospots.nexus.platform.organization.domain.enums.TenantType#ENTERPRISE} 形态允许维护法定档案。
 * 组织形态（企业、团队、个人）由 {@link com.innospots.nexus.platform.organization.domain.enums.TenantType} 区分。</p>
 *
 * <p>分层约定：{@code endpoint} → {@code service}（工作流）→ {@code operator}（单表持久化）→ {@code dao}。
 * 不含 portal 租户内 {@code OrganizationUnit}、不含租户用户 IAM（见 {@code platform.user}）。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint
 * @see com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint
 * @see com.innospots.nexus.platform.organization.service.PlatformTenantService
 * @see com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService
 * @see com.innospots.nexus.base.domain.tenant.TenantSnapshot
 */
package com.innospots.nexus.platform.organization;
