package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Jackson {@link Module} that activates field-level value conversion and masking.
 * <p>
 * Register this module on your {@code ObjectMapper} to enable automatic
 * conversion of fields annotated with {@code @ValueConverter} and masking of
 * fields annotated with {@code @MaskValue} during serialization.
 * Without this module, these annotations are ignored.
 *
 * <pre>{@code
 * ObjectMapper mapper = JsonMapper.builder()
 *     .addModule(new MaskingModule())
 *     .build();
 * }</pre>
 *
 * @see ValueConverter
 * @see MaskValue
 * @see MaskStrategy
 */
public final class MaskingModule extends Module {

    @Override
    public String getModuleName() {
        return "innospots-nexus-masking";
    }

    @Override
    public Version version() {
        return new Version(0, 1, 0, null, "com.innospots", "innospots-nexus-masking");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanSerializerModifier(new MaskingBeanSerializerModifier());
    }

    /**
     * BeanSerializerModifier that rewires properties annotated with
     * {@code @ValueConverter} or {@code @MaskValue} to use dedicated serializers.
     */
    private static final class MaskingBeanSerializerModifier extends BeanSerializerModifier {

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                          BeanDescription beanDesc,
                                                          List<BeanPropertyWriter> beanProperties) {
            List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size());
            for (BeanPropertyWriter writer : beanProperties) {
                ValueConverter converter = writer.getAnnotation(ValueConverter.class);
                MaskValue maskValue = writer.getAnnotation(MaskValue.class);
                if (converter != null && maskValue != null) {
                    writer.assignSerializer(new ValueConvertingSerializer(converter, new MaskedSerializer(maskValue)));
                } else if (converter != null) {
                    writer.assignSerializer(new ValueConvertingSerializer(converter));
                } else if (maskValue != null) {
                    writer.assignSerializer(new MaskedSerializer(maskValue));
                }
                result.add(writer);
            }
            return result;
        }
    }
}
