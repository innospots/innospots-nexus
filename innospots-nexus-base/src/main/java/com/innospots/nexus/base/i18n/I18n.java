package com.innospots.nexus.base.i18n;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jackson annotation that marks a field for automatic i18n translation
 * during serialization. The annotated field's value is passed through
 * {@link I18nConverter#translate(Object)} before being written to JSON.
 */
@Documented
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@JsonSerialize(using = I18n.I18nFieldSerializer.class)
public @interface I18n {

    class I18nFieldSerializer extends JsonSerializer<Object> {

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            generator.writeObject(I18nConverter.translate(value));
        }
    }
}
