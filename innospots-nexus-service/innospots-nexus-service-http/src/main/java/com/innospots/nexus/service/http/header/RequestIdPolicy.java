package com.innospots.nexus.service.http.header;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.innospots.nexus.base.util.Checks;

/**
 * 解析或生成 {@link StandardHeaders#REQUEST_ID}。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class RequestIdPolicy {

    private static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    /**
     * 返回合法入站 requestId，否则生成新值。
     *
     * @param incoming 可选入站值
     * @return requestId
     */
    public String resolve(Optional<String> incoming) {
        Checks.notNull(incoming, "incoming");
        if (incoming.isPresent() && isValid(incoming.get())) {
            return incoming.get();
        }
        return generate();
    }

    /**
     * 判断 {@code value} 是否为允许的 requestId。
     *
     * @param value 待校验值
     * @return 合法时为 {@code true}
     */
    public boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return ALLOWED.matcher(value).matches();
    }

    /**
     * 生成新的 requestId。
     *
     * @return requestId
     */
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
