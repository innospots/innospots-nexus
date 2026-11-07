package com.innospots.nexus.console.scope.tenant.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 租户治理域业务状态码，模块代号 {@code TNT}。
 *
 * <p>只覆盖租户生命周期；企业法定档案的状态码留在运营平台的组织域状态码中。</p>
 *
 * @author Smars
 * @date 2026/11/07
 * @see com.innospots.nexus.console.scope.tenant.operator.TenantOperator
 */
public enum TenantStatusCode implements StatusCode {

    /** 请求的租户 ID 在 {@code nx_tenant} 中不存在。 */
    TENANT_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Tenant was not found", 404),

    /** 租户编码已被占用。 */
    TENANT_CODE_DUPLICATED(
            "0002",
            StatusCategory.DATA_CONSISTENCY,
            "Tenant code already exists",
            409),

    /** 租户编码格式非法（仅允许字母、数字、下划线）。 */
    TENANT_CODE_INVALID_FORMAT(
            "0003",
            StatusCategory.BUSINESS_RULE,
            "Tenant code must contain only letters, digits, and underscores",
            400),

    /** 租户成员关系不存在。 */
    TENANT_MEMBER_NOT_FOUND("0004", StatusCategory.RESOURCE_DATA, "Tenant member was not found", 404),

    /** 该用户已是租户成员。 */
    TENANT_MEMBER_ALREADY_EXISTS(
            "0005",
            StatusCategory.DATA_CONSISTENCY,
            "Tenant member already exists",
            409);

    private static final String MODULE = "TNT";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    TenantStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check tenant configuration", "zh", "请检查租户配置");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
