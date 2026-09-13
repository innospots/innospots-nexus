package com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

import lombok.Getter;

/**
 * Permission declaration supporting a single code, any-of codes, or a detailed object form.
 *
 * <p>YAML examples:</p>
 * <pre>
 * permission: customer:view
 * permission: [customer:edit, customer:admin]
 * permission: { code: customer:edit, denied: disabled }
 * </pre>
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
     * Creates a single permission code declaration.
     *
     * @param code permission code
     * @return permission config
     */
    public static PermissionConfig code(String code) {
        return new PermissionConfig(PermissionKind.CODE, code, List.of(), PermissionDenied.HIDDEN);
    }

    /**
     * Creates an any-of permission declaration.
     *
     * @param codes permission codes
     * @return permission config
     */
    public static PermissionConfig anyOf(List<String> codes) {
        return new PermissionConfig(PermissionKind.ANY_OF, null, codes, PermissionDenied.HIDDEN);
    }

    /**
     * Creates a detailed permission declaration.
     *
     * @param code permission code
     * @param denied denied behavior
     * @return permission config
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
     * Permission declaration shape.
     */
    public enum PermissionKind {
        CODE,
        ANY_OF,
        DETAILED
    }
}
