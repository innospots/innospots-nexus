package com.innospots.nexus.service.runtime.policy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.audit.annotation.Audited;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected;
import com.innospots.nexus.service.contract.policy.annotation.CircuitProtected;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected;
import com.innospots.nexus.service.contract.security.annotation.PublicAccess;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;

/**
 * 从类型与方法注解解析 {@link OperationPolicy}。
 *
 * @author Smars
 * @date 2026/09/15
 * @see DefaultPolicyCatalog
 */
public final class AnnotationPolicyResolver {

    /**
     * 解析 {@code type} 与 {@code method} 上的策略注解。
     *
     * @param type   声明类型
     * @param method 目标方法
     * @return 已解析策略
     */
    public OperationPolicy resolve(Class<?> type, Method method) {
        Checks.notNull(type, "type");
        Checks.notNull(method, "method");
        validate(type, method);

        // 类型级权限在前，方法级在后（去重保留首次出现顺序）
        Set<String> permissions = new LinkedHashSet<>();
        collectPermissions(type, permissions);
        collectPermissions(method, permissions);

        boolean publicAccess = hasPublicAccess(type) || hasPublicAccess(method);
        if (publicAccess) {
            permissions.clear();
        }

        Audited audited = findAudited(type, method);
        return new OperationPolicy(
                permissions,
                resolveResource(method),
                firstNonBlank(rateLimitKey(method), rateLimitKey(type)),
                firstNonBlank(bulkheadKey(method), bulkheadKey(type)),
                firstNonBlank(circuitKey(method), circuitKey(type)),
                firstNonBlank(timeoutKey(method), timeoutKey(type)),
                null,
                audited != null,
                audited == null ? null : audited.action(),
                audited == null ? null : blankToNull(audited.resourceType()),
                audited == null ? null : blankToNull(audited.snapshot()),
                audited == null ? AuditMode.BEST_EFFORT : audited.mode(),
                null);
    }

    /**
     * 校验 {@code PublicAccess} 与权限注解不冲突。
     *
     * @param type   声明类型
     * @param method 目标方法
     */
    public void validate(Class<?> type, Method method) {
        Checks.notNull(type, "type");
        Checks.notNull(method, "method");
        boolean publicAccess = hasPublicAccess(method);
        if (!publicAccess) {
            return;
        }
        if (hasRequiresPermission(method) || hasRequiresPermission(type)) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
    }

    private static void collectPermissions(Class<?> type, Set<String> permissions) {
        RequiresPermission declared = type.getAnnotation(RequiresPermission.class);
        if (declared != null) {
            permissions.addAll(Arrays.asList(declared.value()));
        }
    }

    private static void collectPermissions(Method method, Set<String> permissions) {
        RequiresPermission declared = method.getAnnotation(RequiresPermission.class);
        if (declared != null) {
            permissions.addAll(Arrays.asList(declared.value()));
        }
    }

    private static boolean hasPublicAccess(Class<?> type) {
        return type.getAnnotation(PublicAccess.class) != null;
    }

    private static boolean hasPublicAccess(Method method) {
        return method.getAnnotation(PublicAccess.class) != null;
    }

    private static boolean hasRequiresPermission(Class<?> type) {
        RequiresPermission declared = type.getAnnotation(RequiresPermission.class);
        return declared != null && declared.value().length > 0;
    }

    private static boolean hasRequiresPermission(Method method) {
        RequiresPermission declared = method.getAnnotation(RequiresPermission.class);
        return declared != null && declared.value().length > 0;
    }

    private static String resolveResource(Method method) {
        RequiresPermission declared = method.getAnnotation(RequiresPermission.class);
        if (declared == null) {
            return null;
        }
        return blankToNull(declared.resource());
    }

    private static Audited findAudited(Class<?> type, Method method) {
        Audited audited = method.getAnnotation(Audited.class);
        if (audited != null) {
            return audited;
        }
        return type.getAnnotation(Audited.class);
    }

    private static String rateLimitKey(Method method) {
        RateLimited declared = method.getAnnotation(RateLimited.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String rateLimitKey(Class<?> type) {
        RateLimited declared = type.getAnnotation(RateLimited.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String bulkheadKey(Method method) {
        BulkheadProtected declared = method.getAnnotation(BulkheadProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String bulkheadKey(Class<?> type) {
        BulkheadProtected declared = type.getAnnotation(BulkheadProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String circuitKey(Method method) {
        CircuitProtected declared = method.getAnnotation(CircuitProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String circuitKey(Class<?> type) {
        CircuitProtected declared = type.getAnnotation(CircuitProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String timeoutKey(Method method) {
        TimeoutProtected declared = method.getAnnotation(TimeoutProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String timeoutKey(Class<?> type) {
        TimeoutProtected declared = type.getAnnotation(TimeoutProtected.class);
        return declared == null ? null : blankToNull(declared.value());
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null) {
            return first;
        }
        return second;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
