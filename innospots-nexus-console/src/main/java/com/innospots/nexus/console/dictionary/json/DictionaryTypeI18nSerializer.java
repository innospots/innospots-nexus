package com.innospots.nexus.console.dictionary.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 字典类型名称 API 序列化：始终输出 locale → value 对象，便于管理端编辑多语言。
 */
public class DictionaryTypeI18nSerializer extends StdSerializer<I18nObject> {

    public DictionaryTypeI18nSerializer() {
        super(I18nObject.class);
    }

    @Override
    public void serialize(I18nObject value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        generator.writeStartObject();
        for (Map.Entry<String, String> entry : value.entrySet()) {
            generator.writeFieldName(entry.getKey());
            generator.writeString(entry.getValue());
        }
        generator.writeEndObject();
    }
}
