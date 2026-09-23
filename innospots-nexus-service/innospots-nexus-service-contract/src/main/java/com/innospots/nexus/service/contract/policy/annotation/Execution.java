package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.innospots.nexus.service.contract.invocation.ExecutionMode;

/**
 * 声明被注解方法的执行方式。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ExecutionMode
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Execution {

    /**
     * 执行模式。
     *
     * @return 模式
     */
    ExecutionMode value();
}
