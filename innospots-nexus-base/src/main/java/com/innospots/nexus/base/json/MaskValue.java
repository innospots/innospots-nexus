package com.innospots.nexus.base.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段或访问器在 JSON 序列化时进行脱敏。
 * <p>仅当 {@link MaskingModule} 注册到 Jackson {@code ObjectMapper} 时脱敏才生效。
 * 未注册模块时，该注解被忽略，字段以原始值序列化。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see MaskStrategy
 * @see MaskingModule
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskValue {

    /**
     * 应用的脱敏策略。
     *
     * @return 脱敏策略
     */
    MaskStrategy value() default MaskStrategy.HIDE;

    /**
     * 保留可见的前导字符数。
     * 仅在 {@link #value()} 为 {@link MaskStrategy#CUSTOM} 时使用。
     *
     * @return 前导保留字符数
     */
    int keepHead() default 0;

    /**
     * 保留可见的尾部字符数。
     * 仅在 {@link #value()} 为 {@link MaskStrategy#CUSTOM} 时使用。
     *
     * @return 尾部保留字符数
     */
    int keepTail() default 0;
}
