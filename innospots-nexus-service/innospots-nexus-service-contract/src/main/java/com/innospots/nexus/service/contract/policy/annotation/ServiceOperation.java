package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明方法的<strong>稳定操作标识</strong>（operationId）。
 *
 * <p><strong>用途</strong>：为日志、指标、审计、幂等与超时查找提供与路由实现无关的稳定名称。
 * 写入 {@link com.innospots.nexus.service.contract.invocation.InvocationContext#operationId()}。
 * 未标注时 Spring AOP 默认派生为 {@code 类简单名.方法名}；Bridge 路径可显式传入不同 id。</p>
 *
 * <p><strong>使用场景</strong>：</p>
 * <ul>
 *   <li>治理超时表 {@code timeouts} 以 operationId 为键时的显式命名。</li>
 *   <li>熔断/限流维度中的 {@code operation:…} 标签。</li>
 *   <li>与权限键区分：operationId 描述操作，权限键描述授权能力。</li>
 * </ul>
 *
 * <p><strong>约束</strong>：仅支持方法级；值应稳定、可枚举，避免拼接动态用户输入。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.policy.OperationDescriptor
 * @see com.innospots.nexus.service.contract.invocation.InvocationContext
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceOperation {

    /**
     * 稳定操作标识，用于上下文与配置查找。
     *
     * @return 非空 operationId，建议点分或 kebab 风格业务名
     */
    String value();
}
