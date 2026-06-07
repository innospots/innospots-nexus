package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Jackson serializer that applies field-level masking.
 * <p>
 * This serializer is automatically wired by {@link MaskingModule} and
 * should not be referenced directly.
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
