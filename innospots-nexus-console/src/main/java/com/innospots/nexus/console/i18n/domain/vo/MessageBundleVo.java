package com.innospots.nexus.console.i18n.domain.vo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 单个 namespace 的扁平语言包及其内容版本。
 *
 * @author Smars
 * @date 2026/10/26
 * @param version 词条内容哈希
 * @param messages 完整 key 到文案的不可变映射
 * @see MessageBundleSetVo
 */
@Schema(name = "MessageBundleVo", description = "单个命名空间的扁平语言包")
public record MessageBundleVo(
        @Schema(description = "基于排序后词条内容计算的 SHA-256 版本", required = true)
        String version,
        @Schema(description = "完整 key 到本地化文案的映射", required = true)
        Map<String, String> messages
) {

    public MessageBundleVo {
        messages = Collections.unmodifiableMap(new LinkedHashMap<>(
                messages == null ? Map.of() : messages));
    }
}
