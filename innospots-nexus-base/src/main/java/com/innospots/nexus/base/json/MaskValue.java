package com.innospots.nexus.base.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or accessor for masking during JSON serialization.
 * <p>Masking is activated only when the {@link MaskingModule} is registered
 * on the Jackson {@code ObjectMapper}. Without the module, the annotation
 * is ignored and fields serialize with their original values.</p>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskValue {

    /**
     * The masking strategy to apply.
     */
    MaskStrategy value() default MaskStrategy.HIDE;

    /**
     * Number of leading characters to keep visible.
     * Only used when {@link #value()} is {@link MaskStrategy#CUSTOM}.
     */
    int keepHead() default 0;

    /**
     * Number of trailing characters to keep visible.
     * Only used when {@link #value()} is {@link MaskStrategy#CUSTOM}.
     */
    int keepTail() default 0;
}
