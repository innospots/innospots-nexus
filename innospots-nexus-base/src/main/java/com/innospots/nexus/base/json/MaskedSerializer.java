package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * 应用字段级脱敏的 Jackson 序列化器。
 * <p>
 * 此序列化器由 {@link MaskingModule} 自动装配，不应直接引用。
 */
final class MaskedSerializer extends StdSerializer<Object> {

    private final MaskValue maskValue;

    MaskedSerializer(MaskValue maskValue) {
        super(Object.class);
        this.maskValue = maskValue;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(mask(value.toString(), maskValue));
    }

    private static String mask(String text, MaskValue maskValue) {
        MaskStrategy strategy = maskValue.value();
        if (strategy == MaskStrategy.CUSTOM) {
            return MaskStrategy.mask(text, maskValue.keepHead(), maskValue.keepTail());
        }
        return strategy.apply(text);
    }
}
