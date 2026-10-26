package com.innospots.nexus.console.i18n.domain.vo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Pactor {@code simple-v1} 语言包集合协议。
 *
 * @author Smars
 * @date 2026/10/26
 * @param appId 应用标识
 * @param locale 规范语言代码
 * @param release 应用发布标识
 * @param format 语言包协议版本
 * @param bundles namespace 到语言包的不可变映射
 * @see com.innospots.nexus.console.i18n.endpoint.I18nBundleEndpoint
 */
@Schema(name = "MessageBundleSetVo", description = "Pactor simple-v1 语言包集合")
public record MessageBundleSetVo(
        @Schema(description = "应用标识", required = true, examples = {"console"})
        String appId,
        @Schema(description = "规范语言代码", required = true, examples = {"zh-CN", "en-US"})
        String locale,
        @Schema(description = "应用发布标识", required = true, examples = {"build-release"})
        String release,
        @Schema(description = "语言包协议版本", required = true, examples = {"simple-v1"})
        String format,
        @Schema(description = "命名空间到语言包的映射", required = true)
        Map<String, MessageBundleVo> bundles
) {

    public MessageBundleSetVo {
        bundles = Collections.unmodifiableMap(new LinkedHashMap<>(
                bundles == null ? Map.of() : bundles));
    }
}
