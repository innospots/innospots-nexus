package com.innospots.nexus.spring.service.http.invocation;

import java.lang.reflect.Method;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.policy.annotation.ServiceOperation;

/**
 * 从 {@link ServiceOperation} 或类型/方法名派生稳定操作标识。
 */
public final class ServiceOperationIdResolver {

    private ServiceOperationIdResolver() {
    }

    /**
     * 解析操作标识。
     *
     * @param declaringType 声明类型（通常为 Controller 类或接口）
     * @param method        目标方法
     * @return 非空操作标识
     */
    public static String resolve(Class<?> declaringType, Method method) {
        Checks.notNull(declaringType, "declaringType");
        Checks.notNull(method, "method");
        ServiceOperation declared = method.getAnnotation(ServiceOperation.class);
        if (declared != null && !declared.value().isBlank()) {
            return declared.value().trim();
        }
        return declaringType.getSimpleName() + "." + method.getName();
    }
}
