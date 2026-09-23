package com.innospots.nexus.base.i18n;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.innospots.nexus.base.json.I18nObjectSerializer;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.Map;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * Jackson 注解，标记字段在序列化时自动进行 i18n 翻译。
 * <p>
 * 字典来源二选一（注解优先）：
 * <ul>
 *   <li>{@link #value()} — i18n 键，如 {@code "${app.title}"} 或裸键 {@code app.title}</li>
 *   <li>成员变量值 — {@link I18nObject}、{@code ${key}} 字符串、嵌套 Map/List 等（见 {@link I18nConverter#translate(Object)}）</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see I18nConverter
 */
@Documented
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@JsonSerialize(using = I18n.I18nFieldSerializer.class, nullsUsing = I18n.I18nFieldSerializer.class)
@JsonDeserialize(using = I18n.I18nFieldDeserializer.class)
public @interface I18n {

    /**
     * 可选 i18n 字典键。非空时忽略成员变量内容，仅按该键解析消息。
     *
     * @return 键表达式或裸键；默认空表示从成员变量翻译
     */
    String value() default "";

    /**
     * 对标注字段执行 i18n 翻译的 Jackson 序列化器。
     */
    class I18nFieldSerializer extends JsonSerializer<Object> implements ContextualSerializer {

        private final String annotationKey;
        private final boolean i18nObjectProperty;

        public I18nFieldSerializer() {
            this(null, false);
        }

        private I18nFieldSerializer(String annotationKey, boolean i18nObjectProperty) {
            this.annotationKey = annotationKey;
            this.i18nObjectProperty = i18nObjectProperty;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) {
            if (property == null) {
                return this;
            }
            I18n i18n = property.getAnnotation(I18n.class);
            if (i18n == null) {
                i18n = property.getContextAnnotation(I18n.class);
            }
            String key = null;
            if (i18n != null && StrUtil.isNotBlank(i18n.value())) {
                key = i18n.value().trim();
            }
            boolean i18nObjectProperty = I18nConverter.isI18nObjectType(property.getType().getRawClass());
            return new I18nFieldSerializer(key, i18nObjectProperty);
        }

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            String resolvedAnnotationKey = annotationKey;
            if (value == null && resolvedAnnotationKey == null) {
                resolvedAnnotationKey = currentPropertyAnnotationKey(generator, serializers);
            }
            if (StrUtil.isNotBlank(resolvedAnnotationKey)) {
                generator.writeObject(I18nConverter.translateAnnotatedField(resolvedAnnotationKey, value));
                return;
            }
            if (value instanceof I18nObject object) {
                I18nObjectSerializer.serialize(object, generator);
                return;
            }
            if (i18nObjectProperty) {
                generator.writeNull();
                return;
            }
            generator.writeObject(I18nConverter.translateAnnotatedField(null, value));
        }

        private static String currentPropertyAnnotationKey(JsonGenerator generator, SerializerProvider serializers)
                throws IOException {
            JsonStreamContext context = generator.getOutputContext();
            Object currentValue = context.getCurrentValue();
            String propertyName = context.getCurrentName();
            if (currentValue == null || propertyName == null) {
                return null;
            }
            return serializers.getConfig()
                    .introspect(serializers.constructType(currentValue.getClass()))
                    .findProperties()
                    .stream()
                    .filter(property -> propertyName.equals(property.getName()))
                    .map(BeanPropertyDefinition::getPrimaryMember)
                    .filter(Objects::nonNull)
                    .map(property -> property.getAnnotation(I18n.class))
                    .filter(Objects::nonNull)
                    .map(I18n::value)
                    .filter(StrUtil::isNotBlank)
                    .map(String::trim)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 对标注字段执行 I18nObject 形态识别与反序列化的 Jackson 反序列化器。
     */
    class I18nFieldDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

        private final String annotationKey;
        private final JavaType propertyType;

        public I18nFieldDeserializer() {
            this(null, null);
        }

        private I18nFieldDeserializer(String annotationKey, JavaType propertyType) {
            this.annotationKey = annotationKey;
            this.propertyType = propertyType;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            if (property == null) {
                return this;
            }
            I18n i18n = property.getAnnotation(I18n.class);
            if (i18n == null) {
                i18n = property.getContextAnnotation(I18n.class);
            }
            String key = null;
            if (i18n != null && StrUtil.isNotBlank(i18n.value())) {
                key = i18n.value().trim();
            }
            return new I18nFieldDeserializer(key, property.getType());
        }

        @Override
        public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            if (node.isNull()) {
                return null;
            }
            if (StrUtil.isNotBlank(annotationKey)) {
                return I18nConverter.toKeyExpression(annotationKey);
            }
            Class<?> rawClass = propertyType == null ? Object.class : propertyType.getRawClass();
            if (I18nConverter.isI18nObjectType(rawClass)) {
                return deserializeI18nObject(node);
            }
            if (rawClass == String.class) {
                return deserializeString(node);
            }
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            return mapper.convertValue(node, propertyType);
        }

        private static Object deserializeI18nObject(JsonNode node) {
            if (node.isTextual()) {
                return I18nConverter.parseI18nObject(node.asText());
            }
            if (node.isObject()) {
                Map<String, String> values = new java.util.LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> {
                    JsonNode valueNode = entry.getValue();
                    if (!valueNode.isNull()) {
                        values.put(entry.getKey(), valueNode.asText());
                    }
                });
                return I18nConverter.parseI18nObject(values);
            }
            throw new IllegalArgumentException("Unsupported JSON shape for I18nObject field");
        }

        private static String deserializeString(JsonNode node) {
            if (node.isTextual()) {
                return node.asText();
            }
            if (node.isObject() && I18nConverter.isI18nObjectShape(toObjectMap(node))) {
                return I18nConverter.parseI18nObject(toObjectMap(node)).value(I18nConverter.locale());
            }
            throw new IllegalArgumentException("Unsupported JSON shape for @I18n String field");
        }

        private static Map<String, Object> toObjectMap(JsonNode node) {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
            return map;
        }
    }
}
