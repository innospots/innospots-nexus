package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.innospots.nexus.base.exception.NexusException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * 应用字段级值转换的 Jackson 序列化器。
 * <p>
 * 可提供委托序列化器来序列化转换后的值，例如在转换后再应用脱敏。
 */
final class ValueConvertingSerializer extends StdSerializer<Object> {

    private final Function<Object, Object> converter;
    private final JsonSerializer<Object> delegate;

    ValueConvertingSerializer(ValueConverter valueConverter) {
        this(valueConverter, null);
    }

    ValueConvertingSerializer(ValueConverter valueConverter, JsonSerializer<Object> delegate) {
        super(Object.class);
        this.converter = createConverter(valueConverter);
        this.delegate = delegate;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Object converted = converter.apply(value);
        if (converted == null) {
            gen.writeNull();
            return;
        }
        if (delegate != null) {
            delegate.serialize(converted, gen, provider);
            return;
        }
        provider.defaultSerializeValue(converted, gen);
    }

    @SuppressWarnings("unchecked")
    private static Function<Object, Object> createConverter(ValueConverter valueConverter) {
        try {
            return (Function<Object, Object>) valueConverter.value().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw NexusException.build("JSON_VALUE_CONVERTER_CREATE_FAILED",
                    "Failed to create JSON value converter: " + valueConverter.value().getName(), e);
        }
    }
}
