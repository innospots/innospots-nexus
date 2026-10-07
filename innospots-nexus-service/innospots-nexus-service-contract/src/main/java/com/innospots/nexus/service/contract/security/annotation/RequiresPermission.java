package com.innospots.nexus.service.contract.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为资源或方法声明所需权限键。应标注于 Controller/Resource 类型。
 *
 * @author Smars
 * @date 2026/09/13
 * @see PublicAccess
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresPermission {

    /**
     * 所需权限键。与类型级键以 AND 组合。
     *
     * @return 权限键
     */
    String[] value();

    /**
     * 资源解析器键。为空表示使用该操作的默认解析器。
     *
     * @return 解析器键
     */
    String resource() default "";
}
