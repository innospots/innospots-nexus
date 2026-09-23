package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;

import java.io.IOException;
import java.util.Map;

/**
 * {@link I18nObject} 的 Jackson 序列化器。
 * <p>
 * 默认按当前线程 {@link I18nConverter#locale()} 输出单个本地化字符串；
 * 当 {@link I18nConverter#shouldIgnoreI18n()} 为 {@code true} 时，按 locale → value 映射输出 JSON 对象。
 *
 * @author Smars
 * @date 2026/09/16
 * @see I18nObject
 * @see I18nConverter
 */
public class I18nObjectSerializer extends StdSerializer<I18nObject> {

    private static final I18nObjectSerializer INSTANCE = new I18nObjectSerializer();

    public I18nObjectSerializer() {
        super(I18nObject.class);
    }

    /**
     * 供 Jackson 或组合序列化器复用的单例实例。
     *
     * @return 序列化器实例
     */
    public static I18nObjectSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void serialize(I18nObject value, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
        serialize(value, generator);
    }

    /**
     * 将 {@link I18nObject} 写入 JSON。
     *
     * @param value     待序列化的对象；为 null 时写入 JSON null
     * @param generator JSON 生成器
     * @throws IOException 写入失败时
     */
    public static void serialize(I18nObject value, JsonGenerator generator) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        if (I18nConverter.shouldIgnoreI18n()) {
            generator.writeStartObject();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                generator.writeFieldName(entry.getKey());
                generator.writeString(entry.getValue());
            }
            generator.writeEndObject();
            return;
        }
        String message = value.value(I18nConverter.locale());
        if (message == null) {
            generator.writeNull();
        } else {
            generator.writeString(message);
        }
    }
}
