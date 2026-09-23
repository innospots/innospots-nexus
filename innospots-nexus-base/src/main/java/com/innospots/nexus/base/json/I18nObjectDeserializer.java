package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link I18nObject} 的 Jackson 反序列化器。
 * <p>
 * 支持 JSON 字符串（单语言默认值）与 locale → value 的 JSON 对象两种形态。
 *
 * @author Smars
 * @date 2026/09/16
 * @see I18nObject
 * @see I18nConverter#parseI18nObject(Object)
 */
public class I18nObjectDeserializer extends StdDeserializer<I18nObject> {

    private static final I18nObjectDeserializer INSTANCE = new I18nObjectDeserializer();

    public I18nObjectDeserializer() {
        super(I18nObject.class);
    }

    /**
     * 供 Jackson 或组合反序列化器复用的单例实例。
     *
     * @return 反序列化器实例
     */
    public static I18nObjectDeserializer getInstance() {
        return INSTANCE;
    }

    @Override
    public I18nObject deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_STRING) {
            return I18nConverter.parseI18nObject(parser.getText());
        }
        if (token == JsonToken.START_OBJECT) {
            Map<String, Object> raw = new LinkedHashMap<>();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();
                if (parser.currentToken() == JsonToken.VALUE_NULL) {
                    continue;
                }
                if (parser.currentToken() != JsonToken.VALUE_STRING) {
                    return (I18nObject) context.handleUnexpectedToken(
                            I18nObject.class,
                            parser.currentToken(),
                            parser,
                            "I18nObject values must be strings"
                    );
                }
                raw.put(fieldName, parser.getText());
            }
            return I18nConverter.parseI18nObject(raw);
        }
        return (I18nObject) context.handleUnexpectedToken(I18nObject.class, parser);
    }
}
