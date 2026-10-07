package com.innospots.nexus.base.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * 标记字段或访问器在 JSON 序列化时进行值转换。
 * <p>转换器类必须提供公共无参构造函数。
 * 它接收原始字段值并返回 Jackson 应序列化的值。
 * 转换后的值可选择通过委托序列化器（如 {@link MaskedSerializer}）进行链式处理。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see MaskingModule
 * @see ValueConvertingSerializer
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueConverter {

    /**
     * 用于转换原始值的函数实现类。
     *
     * @return 转换器类
     */
    Class<? extends Function<?, ?>> value();
}
