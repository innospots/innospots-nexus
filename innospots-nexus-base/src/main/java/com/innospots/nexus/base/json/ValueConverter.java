package com.innospots.nexus.base.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * Marks a field or accessor for value conversion during JSON serialization.
 * <p>The converter class must provide a public no-argument constructor.
 * It receives the original field value and returns the value that Jackson
 * should serialize. Converted values may optionally pass through a
 * delegate serializer (e.g. {@link MaskedSerializer}) for chained
 * processing.</p>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueConverter {

    /**
     * Function implementation used to transform the original value.
     */
    Class<? extends Function<?, ?>> value();
}
