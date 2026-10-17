package com.innospots.nexus.console.logger.support;

import com.innospots.nexus.console.logger.AuditLog;

/**
 * 宿主在响应阶段组装的不可变审计调用元数据。
 *
 * @author Smars
 * @date 2026/10/17
 * @param auditLog 审计注解
 * @param className 资源实现类名
 * @param methodName 被审计方法名
 * @param httpMethod HTTP 方法
 * @param httpPath 请求路径
 * @param startTimeMillis 请求开始时间（epoch 毫秒）
 * @see AuditLogRecorder
 */
public record AuditLogInvocationState(
        AuditLog auditLog,
        String className,
        String methodName,
        String httpMethod,
        String httpPath,
        long startTimeMillis
) {

    public AuditLogInvocationState {
        className = className == null ? "" : className;
        methodName = methodName == null ? "" : methodName;
        httpMethod = httpMethod == null ? "" : httpMethod;
        httpPath = httpPath == null ? "" : httpPath;
    }
}
