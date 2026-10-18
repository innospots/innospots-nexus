package com.innospots.nexus.console.auth.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 注册策略门禁相关状态码（模块 {@code REG}）。
 *
 * @author Smars
 * @date 2026/10/18
 * @see com.innospots.nexus.console.auth.service.RegistrationPolicyService#requireSelfServiceMode
 */
public enum RegistrationPolicyStatusCode implements StatusCode {

    /** 当前 {@code nexus.console.registration.mode} 不允许请求的注册流程。 */
    REGISTRATION_MODE_DISABLED(
            "0001",
            StatusCategory.BUSINESS_RULE,
            "Registration is not enabled for the current platform mode",
            403);

    /** 状态码模块前缀。 */
    private static final String MODULE = "REG";

    /** 模块内序号。 */
    private final String localCode;

    /** 状态分类。 */
    private final StatusCategory category;

    /** 用户可见消息。 */
    private final I18nObject message;

    /** 建议 HTTP 状态码。 */
    private final int httpStatusCode;

    RegistrationPolicyStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of(
                "en",
                message,
                "zh",
                "当前平台注册模式不允许此操作，请检查注册方式配置");
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
        return I18nObject.of("en", "Check nexus.console.registration settings", "zh", "请检查 nexus.console.registration 注册配置");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
