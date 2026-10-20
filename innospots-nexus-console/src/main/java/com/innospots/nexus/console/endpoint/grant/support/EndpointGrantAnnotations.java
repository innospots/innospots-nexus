package com.innospots.nexus.console.endpoint.grant.support;

import java.lang.reflect.Method;

import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;

/**
 * 从 JAX-RS 资源实现方法与 console 契约接口解析 {@link EndpointGrant}。
 */
public final class EndpointGrantAnnotations {

    private EndpointGrantAnnotations() {
    }

    /**
     * 解析有效 grant。
     *
     * @param implementationMethod 资源实现方法
     * @param resourceClass        资源实现类
     * @return 合并后的策略；未标注时为 null
     */
    /**
     * 解析资源类型上的 {@link EndpointModule}（本类或实现的契约接口）。
     *
     * @param resourceClass JAX-RS 资源实现类
     * @return 模块注解；未声明时为 null
     */
    public static EndpointModule resolveModule(Class<?> resourceClass) {
        if (resourceClass == null) {
            return null;
        }
        EndpointModule direct = resourceClass.getAnnotation(EndpointModule.class);
        if (direct != null) {
            return direct;
        }
        for (Class<?> iface : resourceClass.getInterfaces()) {
            EndpointModule onInterface = iface.getAnnotation(EndpointModule.class);
            if (onInterface != null) {
                return onInterface;
            }
        }
        return null;
    }

    /**
     * 是否应纳入 grant 目录（声明了 {@link EndpointModule} 的运行时资源类型）。
     *
     * @param resourceClass 候选类型
     * @return 本类或接口上存在 {@link EndpointModule} 时为 true
     */
    public static boolean isGrantResourceType(Class<?> resourceClass) {
        return resolveModule(resourceClass) != null;
    }

    public static ResolvedEndpointGrant resolve(Method implementationMethod, Class<?> resourceClass) {
        if (implementationMethod == null) {
            return null;
        }
        EndpointModule module = resolveModuleForMethod(resourceClass, implementationMethod);
        EndpointGrant grant = findGrantOnType(implementationMethod, implementationMethod.getDeclaringClass());
        if (grant == null && resourceClass != null) {
            for (Class<?> iface : resourceClass.getInterfaces()) {
                grant = findGrantOnInterface(iface, implementationMethod);
                if (grant != null) {
                    break;
                }
            }
        }
        if (grant == null && resourceClass == null) {
            Class<?> declaring = implementationMethod.getDeclaringClass();
            if (declaring.isInterface()) {
                grant = findGrantOnInterface(declaring, implementationMethod);
            }
        }
        if (grant == null) {
            return null;
        }
        return merge(
                module,
                grant.name(),
                grant.label(),
                grant.recordArgs(),
                grant.auditLog(),
                grant.ignorePermission(),
                grant.ignoreAuth(),
                grant.action());
    }

    private static ResolvedEndpointGrant merge(
            EndpointModule module,
            String name,
            I18nLiteral label,
            boolean recordArgs,
            boolean auditLog,
            boolean ignorePermission,
            boolean ignoreAuth,
            EndpointGrantAction action
    ) {
        String moduleKey = module == null ? "" : module.value();
        boolean moduleIgnorePermission = module != null && module.ignorePermission();
        boolean moduleIgnoreAuth = module != null && module.ignoreAuth();
        return new ResolvedEndpointGrant(
                moduleKey,
                action,
                name,
                label,
                recordArgs,
                auditLog,
                moduleIgnorePermission || ignorePermission,
                moduleIgnoreAuth || ignoreAuth);
    }

    private static EndpointModule resolveModuleForMethod(Class<?> resourceClass, Method implementationMethod) {
        EndpointModule module = resolveModule(resourceClass);
        if (module != null) {
            return module;
        }
        if (implementationMethod != null) {
            Class<?> declaring = implementationMethod.getDeclaringClass();
            if (declaring.isInterface()) {
                return declaring.getAnnotation(EndpointModule.class);
            }
        }
        return null;
    }

    private static EndpointGrant findGrantOnType(Method implementationMethod, Class<?> type) {
        if (type == null) {
            return null;
        }
        try {
            Method declared = type.getMethod(
                    implementationMethod.getName(),
                    implementationMethod.getParameterTypes());
            return declared.getAnnotation(EndpointGrant.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static EndpointGrant findGrantOnInterface(Class<?> iface, Method implementationMethod) {
        try {
            Method declared = iface.getMethod(
                    implementationMethod.getName(),
                    implementationMethod.getParameterTypes());
            return declared.getAnnotation(EndpointGrant.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
