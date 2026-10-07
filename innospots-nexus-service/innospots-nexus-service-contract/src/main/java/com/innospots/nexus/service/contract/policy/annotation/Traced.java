package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建可选子 Span。默认请求追踪无需此注解。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.trace.TraceProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Traced {

    /**
     * 子 Span 名称。
     *
     * @return Span 名称
     */
    String value();
}
