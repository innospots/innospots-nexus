package com.innospots.nexus.console.i18n.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.status.StatusCodeRules;

/**
 * 控制台语言包接口的状态码（模块 {@code LOC}）。
 *
 * @author Smars
 * @date 2026/10/26
 * @see com.innospots.nexus.console.i18n.service.MessageBundleService
 */
public enum I18nStatusCode implements StatusCode {

    /** 显式指定的语言不受支持。 */
    LOCALE_NOT_SUPPORTED(
            "0001",
            StatusCategory.INPUT_VALIDATION,
            "The requested locale is not supported",
            "不支持请求的语言",
            "Use zh-CN or en-US",
            "请使用 zh-CN 或 en-US",
            400),
    /** namespace 不在 Console 语言包目录中。 */
    NAMESPACE_NOT_SUPPORTED(
            "0002",
            StatusCategory.INPUT_VALIDATION,
            "The requested message namespace is not supported",
            "不支持请求的语言包命名空间",
            "Request only namespaces published by the console application",
            "请仅请求控制台应用已发布的命名空间",
            400),
    /** appId 与当前语言包应用不一致。 */
    APP_ID_NOT_SUPPORTED(
            "0003",
            StatusCategory.INPUT_VALIDATION,
            "The requested application is not supported",
            "不支持请求的应用标识",
            "Use appId=console",
            "请使用 appId=console",
            400),
    /** 声明的语言包资源缺失。 */
    BUNDLE_NOT_FOUND(
            "0004",
            StatusCategory.RESOURCE_DATA,
            "A requested message bundle was not found",
            "请求的语言包不存在",
            "Verify that the namespace resources are packaged",
            "请确认对应命名空间资源已随应用打包",
            404),
    /** 语言包资源无法读取或解析。 */
    BUNDLE_LOAD_FAILED(
            "0005",
            StatusCategory.INTERNAL_ERROR,
            "Message bundles could not be loaded",
            "语言包加载失败",
            "Contact the administrator or retry later",
            "请联系管理员或稍后重试",
            500);

    private static final String MODULE = "LOC";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final I18nObject advice;
    private final int httpStatusCode;

    I18nStatusCode(
            String localCode,
            StatusCategory category,
            String messageEn,
            String messageZh,
            String adviceEn,
            String adviceZh,
            int httpStatusCode
    ) {
        StatusCodeRules.requireValid(MODULE, category, localCode);
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of("en", messageEn, "zh", messageZh);
        this.advice = I18nObject.of("en", adviceEn, "zh", adviceZh);
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
        return advice;
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
