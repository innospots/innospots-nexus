package com.innospots.nexus.base.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ValueConverterTest {

    static final class UpperCaseConverter implements Function<Object, Object> {
        @Override
        public Object apply(Object value) {
            return value == null ? null : value.toString().toUpperCase();
        }
    }

    static final class PrefixConverter implements Function<Object, Object> {
        @Override
        public Object apply(Object value) {
            return "PX-" + value;
        }
    }

    static final class ReversingConverter implements Function<Object, Object> {
        @Override
        public Object apply(Object value) {
            return value == null ? null : new StringBuilder(value.toString()).reverse().toString();
        }
    }

    static final class NullReturningConverter implements Function<Object, Object> {
        @Override
        public Object apply(Object value) {
            return null;
        }
    }

    record ConvertedBean(
            @ValueConverter(UpperCaseConverter.class) String name,
            @ValueConverter(PrefixConverter.class) String label,
            @ValueConverter(ReversingConverter.class) String reversed,
            String raw
    ) {
    }

    record NullConvertedBean(
            @ValueConverter(UpperCaseConverter.class) String value
    ) {
    }

    record NullReturningBean(
            @ValueConverter(NullReturningConverter.class) String value
    ) {
    }

    private static final ObjectMapper MASKED_MAPPER = JsonMapper.builder()
            .addModule(new MaskingModule())
            .build();

    @Nested
    class ConverterDirectTests {

        @Test
        void upperCaseConverterWorks() {
            assertThat(new UpperCaseConverter().apply("hello")).isEqualTo("HELLO");
        }

        @Test
        void upperCaseConverterNull() {
            assertThat(new UpperCaseConverter().apply(null)).isNull();
        }

        @Test
        void prefixConverterWorks() {
            assertThat(new PrefixConverter().apply("test")).isEqualTo("PX-test");
        }

        @Test
        void reversingConverterWorks() {
            assertThat(new ReversingConverter().apply("abc")).isEqualTo("cba");
        }

        @Test
        void nullReturningConverterReturnsNull() {
            assertThat(new NullReturningConverter().apply("anything")).isNull();
        }
    }

    @Nested
    class AnnotationSerializationTests {

        @Test
        void convertsAllAnnotatedFields() throws Exception {
            ConvertedBean bean = new ConvertedBean("hello", "world", "abc", "raw-value");

            String json = MASKED_MAPPER.writeValueAsString(bean);

            assertThat(json).contains("\"name\":\"HELLO\"");
            assertThat(json).contains("\"label\":\"PX-world\"");
            assertThat(json).contains("\"reversed\":\"cba\"");
            assertThat(json).contains("\"raw\":\"raw-value\"");
        }

        @Test
        void nullFieldPassesThrough() throws Exception {
            String json = MASKED_MAPPER.writeValueAsString(new NullConvertedBean(null));

            assertThat(json).contains("\"value\":null");
        }

        @Test
        void nullReturningConverterWritesNull() throws Exception {
            String json = MASKED_MAPPER.writeValueAsString(new NullReturningBean("anything"));

            assertThat(json).contains("\"value\":null");
        }

        @Test
        void regularMapperDoesNotConvert() throws Exception {
            ConvertedBean bean = new ConvertedBean("hello", "world", "abc", "raw-value");

            String json = Jsons.toJson(bean);

            assertThat(json).contains("\"name\":\"hello\"");
            assertThat(json).contains("\"label\":\"world\"");
            assertThat(json).contains("\"reversed\":\"abc\"");
            assertThat(json).contains("\"raw\":\"raw-value\"");
        }
    }
}
