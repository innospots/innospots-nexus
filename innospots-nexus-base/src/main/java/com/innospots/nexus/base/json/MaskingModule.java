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
 * 激活字段级值转换与脱敏的 Jackson {@link Module}。
 * <p>
 * 将此模块注册到 {@code ObjectMapper} 以在序列化时自动转换标注了
 * {@code @ValueConverter} 的字段并脱敏标注了 {@code @MaskValue} 的字段。
 * 未注册此模块时，这些注解被忽略。
 *
 * <pre>{@code
 * ObjectMapper mapper = JsonMapper.builder()
 *     .addModule(new MaskingModule())
 *     .build();
 * }</pre>
 *
 * @author Smars
 * @date 2026/09/13
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
     * 将标注了 {@code @ValueConverter} 或 {@code @MaskValue} 的属性
     * 重定向到专用序列化器的 BeanSerializerModifier。
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
