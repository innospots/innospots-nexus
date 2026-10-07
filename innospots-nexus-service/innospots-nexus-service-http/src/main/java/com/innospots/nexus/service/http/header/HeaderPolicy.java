package com.innospots.nexus.service.http.header;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * 请求头基线校验。重复且不一致的安全敏感头将被拒绝。
 *
 * @author Smars
 * @date 2026/09/15
 * @see StandardHeaders
 */
public final class HeaderPolicy {

    private static final Set<String> DUPLICATE_SENSITIVE = Set.of(
            StandardHeaders.AUTHORIZATION,
            StandardHeaders.CONTENT_LENGTH);

    /**
     * 校验 {@code headers} 是否满足服务头策略。
     *
     * @param headers 小写头名到值列表的映射
     */
    public void validate(Map<String, List<String>> headers) {
        Checks.notNull(headers, "headers");
        for (String headerName : DUPLICATE_SENSITIVE) {
            List<String> values = headers.get(headerName);
            if (values == null || values.size() <= 1) {
                continue;
            }
            String first = values.getFirst();
            for (String value : values) {
                if (!first.equals(value)) {
                    throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
                }
            }
        }
    }

    /**
     * 规范化头名用于查找。
     *
     * @param headerName 原始头名
     * @return 小写头名
     */
    public String normalizeName(String headerName) {
        Checks.notBlank(headerName, "headerName");
        return headerName.toLowerCase(Locale.ROOT);
    }
}
