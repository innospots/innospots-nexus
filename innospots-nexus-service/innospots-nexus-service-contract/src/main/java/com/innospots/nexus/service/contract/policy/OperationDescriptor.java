package com.innospots.nexus.service.contract.policy;

import java.lang.reflect.Type;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;

/**
 * 启动时捕获的静态操作元数据。
 *
 * @param operationId         稳定操作标识
 * @param routeTemplate       路由模板
 * @param transport           传输名称，如 http 或 websocket
 * @param declaredResultType  声明的结果类型
 * @param executionMode       执行模式
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 */
public record OperationDescriptor(
        String operationId,
        String routeTemplate,
        String transport,
        Type declaredResultType,
        ExecutionMode executionMode
) {

    public OperationDescriptor {
        Checks.notBlank(operationId, "operationId");
        Checks.notBlank(transport, "transport");
        Checks.notNull(declaredResultType, "declaredResultType");
        Checks.notNull(executionMode, "executionMode");
        routeTemplate = routeTemplate == null || routeTemplate.isBlank() ? null : routeTemplate;
    }
}
