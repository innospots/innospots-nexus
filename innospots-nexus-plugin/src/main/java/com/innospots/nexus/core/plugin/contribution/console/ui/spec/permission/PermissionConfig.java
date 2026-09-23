package com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import lombok.Getter;

/**
 * 权限声明，支持单个代码、任一匹配代码数组或详细对象形式。
 *
 * <p>YAML 示例：</p>
 * <pre>
 * permission: customer:view
 * permission: [customer:edit, customer:admin]
 * permission: { code: customer:edit, denied: disabled }
 * </pre>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonDeserialize(using = PermissionConfigDeserializer.class)
public final class PermissionConfig {

    private final PermissionKind kind;
    private final String code;
    private final List<String> anyOf;
    private final PermissionDenied denied;

    private PermissionConfig(
            PermissionKind kind,
            String code,
            List<String> anyOf,
            PermissionDenied denied
    ) {
        this.kind = kind;
        this.code = code;
        this.anyOf = anyOf == null ? List.of() : List.copyOf(anyOf);
        this.denied = denied;
    }

    /**
     * 创建单个权限代码声明。
     *
     * @param code 权限代码
     * @return 权限配置
     */
    public static PermissionConfig code(String code) {
        return new PermissionConfig(PermissionKind.CODE, code, List.of(), PermissionDenied.HIDDEN);
    }

    /**
     * 创建任一匹配权限声明。
     *
     * @param codes 权限代码列表
     * @return 权限配置
     */
    public static PermissionConfig anyOf(List<String> codes) {
        return new PermissionConfig(PermissionKind.ANY_OF, null, codes, PermissionDenied.HIDDEN);
    }

    /**
     * 创建详细权限声明。
     *
     * @param code 权限代码
     * @param denied 拒绝时的行为
     * @return 权限配置
     */
    public static PermissionConfig detailed(String code, PermissionDenied denied) {
        return new PermissionConfig(PermissionKind.DETAILED, code, List.of(), denied);
    }

    static PermissionConfig fromCode(String code) {
        return code(code);
    }

    static PermissionConfig fromAnyOf(List<String> codes) {
        return anyOf(codes);
    }

    static PermissionConfig fromDetailed(String code, PermissionDenied denied) {
        return detailed(code, denied == null ? PermissionDenied.HIDDEN : denied);
    }

    /**
     * 权限声明形态。
     */
    public enum PermissionKind {

        /** 单个权限代码。 */
        CODE,

        /** 任一匹配权限代码。 */
        ANY_OF,

        /** 带拒绝行为的详细声明。 */
        DETAILED
    }
}
