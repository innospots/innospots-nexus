package com.innospots.nexus.platform.organization.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 平台组织域（租户与企业档案）业务状态码，模块代号 {@code PLO}。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.operator.TenantOperator
 * @see com.innospots.nexus.platform.organization.operator.EnterpriseOperator
 */
public enum PlatformOrganizationStatusCode implements StatusCode {

    /** 请求的租户 ID 在 {@code nx_pl_tenant} 中不存在。 */
    PLATFORM_TENANT_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Platform tenant was not found", 404),

    /** 租户下尚未配置企业法定档案。 */
    ENTERPRISE_PROFILE_NOT_FOUND("0002", StatusCategory.RESOURCE_DATA, "Enterprise profile was not found", 404),

    /** 同一租户已存在企业档案，禁止重复插入。 */
    ENTERPRISE_PROFILE_ALREADY_EXISTS(
            "0003",
            StatusCategory.DATA_CONSISTENCY,
            "Enterprise profile already exists for tenant",
            409),

    /** 非企业形态租户不得维护法定企业档案。 */
    ENTERPRISE_PROFILE_TENANT_TYPE_MISMATCH(
            "0004",
            StatusCategory.BUSINESS_RULE,
            "Enterprise profile applies only to ENTERPRISE tenant type",
            409),

    /** 租户编码已被占用。 */
    TENANT_CODE_DUPLICATED(
            "0005",
            StatusCategory.DATA_CONSISTENCY,
            "Tenant code already exists",
            409);

    private static final String MODULE = "PLO";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformOrganizationStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of("en", message, "zh", message);
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String module() {
        return MODULE;
    }

    @Override
    public String localCode() {
        return localCode;
    }

    @Override
    public StatusCategory category() {
        return category;
    }

    @Override
    public I18nObject message() {
        return message;
    }

    @Override
    public I18nObject advice() {
        return I18nObject.of("en", "Check tenant or enterprise profile", "zh", "请检查租户或企业档案");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
