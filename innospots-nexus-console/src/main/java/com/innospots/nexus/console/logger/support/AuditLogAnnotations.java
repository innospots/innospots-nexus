package com.innospots.nexus.console.logger.support;

import java.lang.reflect.Method;

import com.innospots.nexus.console.logger.AuditLog;

/**
 * 从 JAX-RS 资源类/接口解析 {@link AuditLog}。
 */
public final class AuditLogAnnotations {

    private AuditLogAnnotations() {
    }

    /**
     * 优先读取实现方法上的注解，再回退到声明接口上的同名方法。
     *
     * @param implementationMethod 资源实现方法
     * @param resourceClass        资源类
     * @return 审计注解；未标注时为 null
     */
    public static AuditLog resolve(Method implementationMethod, Class<?> resourceClass) {
        if (implementationMethod == null) {
            return null;
        }
        AuditLog direct = implementationMethod.getAnnotation(AuditLog.class);
        if (direct != null) {
            return direct;
        }
        if (resourceClass == null) {
            return null;
        }
        for (Class<?> iface : resourceClass.getInterfaces()) {
            AuditLog onInterface = findOnInterface(iface, implementationMethod);
            if (onInterface != null) {
                return onInterface;
            }
        }
        return null;
    }

    private static AuditLog findOnInterface(Class<?> iface, Method implementationMethod) {
        try {
            Method declared = iface.getMethod(
                    implementationMethod.getName(),
                    implementationMethod.getParameterTypes());
            return declared.getAnnotation(AuditLog.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
