package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskValueTest {

    record MaskedBean(
            @MaskValue(MaskStrategy.PHONE) String phone,
            @MaskValue(MaskStrategy.EMAIL) String email,
            @MaskValue(MaskStrategy.ID_CARD) String idCard,
            @MaskValue(MaskStrategy.BANK_CARD) String bankCard,
            @MaskValue(MaskStrategy.NAME) String name,
            @MaskValue(MaskStrategy.PASSWORD) String password,
            @MaskValue String hidden,
            @MaskValue(value = MaskStrategy.CUSTOM, keepHead = 2, keepTail = 2) String customField,
            @MaskValue(MaskStrategy.NAME) String shortName,
            String plain
    ) {
    }

    record NullableBean(
            @MaskValue(MaskStrategy.PHONE) String phone
    ) {
    }

    private static final ObjectMapper MASKED_MAPPER = JsonMapper.builder()
            .addModule(new MaskingModule())
            .build();

    @Nested
    class StrategyDirectTests {

        @Test
        void phone() {
            assertThat(MaskStrategy.PHONE.apply("13812345678")).isEqualTo("138****5678");
        }

        @Test
        void phoneShort() {
            assertThat(MaskStrategy.PHONE.apply("12")).isEqualTo("12");
        }

        @Test
        void email() {
            assertThat(MaskStrategy.EMAIL.apply("user@domain.com")).isEqualTo("u***@domain.com");
        }

        @Test
        void emailNoAt() {
            assertThat(MaskStrategy.EMAIL.apply("plaintext")).isEqualTo("***");
        }

        @Test
        void idCard() {
            assertThat(MaskStrategy.ID_CARD.apply("320102199001011234")).isEqualTo("320****1234");
        }

        @Test
        void bankCard() {
            assertThat(MaskStrategy.BANK_CARD.apply("6222021234567890")).isEqualTo("6222****7890");
        }

        @Test
        void nameTwoChars() {
            assertThat(MaskStrategy.NAME.apply("张三")).isEqualTo("张*");
        }

        @Test
        void nameThreeChars() {
            assertThat(MaskStrategy.NAME.apply("张三丰")).isEqualTo("张三*");
        }

        @Test
        void nameLong() {
            assertThat(MaskStrategy.NAME.apply("欧阳娜娜")).isEqualTo("欧阳**");
        }

        @Test
        void nameEmpty() {
            assertThat(MaskStrategy.NAME.apply("")).isEqualTo("***");
        }

        @Test
        void password() {
            assertThat(MaskStrategy.PASSWORD.apply("anyvalue")).isEqualTo("******");
        }

        @Test
        void hide() {
            assertThat(MaskStrategy.HIDE.apply("anything")).isEqualTo("***");
        }

        @Test
        void nullInput() {
            assertThat(MaskStrategy.PHONE.apply(null)).isNull();
            assertThat(MaskStrategy.EMAIL.apply(null)).isNull();
            assertThat(MaskStrategy.NAME.apply(null)).isNull();
            assertThat(MaskStrategy.PASSWORD.apply(null)).isNull();
            assertThat(MaskStrategy.HIDE.apply(null)).isNull();
        }
    }

    @Nested
    class AnnotationSerializationTests {

        @Test
        void masksAllStrategies() throws Exception {
            MaskedBean bean = new MaskedBean(
                    "13812345678", "admin@nexus.com",
                    "320102199001011234", "6222021234567890",
                    "张三丰", "s3cret!", "hide-me",
                    "CUSTOM-FIELD", "ab", "plain-text"
            );

            String json = MASKED_MAPPER.writeValueAsString(bean);

            assertThat(json).contains("\"phone\":\"138****5678\"");
            assertThat(json).contains("\"email\":\"a***@nexus.com\"");
            assertThat(json).contains("\"idCard\":\"320****1234\"");
            assertThat(json).contains("\"bankCard\":\"6222****7890\"");
            assertThat(json).contains("\"name\":\"张三*\"");
            assertThat(json).contains("\"password\":\"******\"");
            assertThat(json).contains("\"hidden\":\"***\"");
            assertThat(json).contains("\"customField\":\"CU****LD\"");
            assertThat(json).contains("\"plain\":\"plain-text\"");
        }

        @Test
        void shortValueNotMasked() throws Exception {
            MaskedBean bean = new MaskedBean(
                    "12", "a@b", "12", "12",
                    "ab", "pw", "x", "z", "ab", "x"
            );

            String json = MASKED_MAPPER.writeValueAsString(bean);

            assertThat(json).contains("\"shortName\":\"a*\"");
        }

        @Test
        void nullFieldsSerializedAsNull() throws Exception {
            String json = MASKED_MAPPER.writeValueAsString(new NullableBean(null));

            assertThat(json).contains("\"phone\":null");
        }

        @Test
        void regularMapperIgnoresAnnotation() throws Exception {
            MaskedBean bean = new MaskedBean(
                    "13812345678", "admin@nexus.com",
                    "320102199001011234", "6222021234567890",
                    "张三丰", "s3cret!", "hide-me",
                    "CUSTOM-FIELD", "ab", "plain-text"
            );

            String json = Jsons.toJson(bean);

            assertThat(json).contains("\"phone\":\"13812345678\"");
            assertThat(json).contains("\"name\":\"张三丰\"");
            assertThat(json).doesNotContain("***");
        }
    }

    @Nested
    class CustomStrategyTests {

        @Test
        void customWithKeepHeadKeepTail() throws Exception {
            var mapper = JsonMapper.builder()
                    .addModule(new MaskingModule())
                    .build();
            record CustomBean(@MaskValue(value = MaskStrategy.CUSTOM, keepHead = 1, keepTail = 3) String value) {
            }

            String json = mapper.writeValueAsString(new CustomBean("abcdefgh"));

            assertThat(json).contains("\"value\":\"a****fgh\"");
        }

        @Test
        void customWithZeroKeep() throws Exception {
            record ZeroBean(@MaskValue(value = MaskStrategy.CUSTOM, keepHead = 0, keepTail = 0) String value) {
            }

            String json = MASKED_MAPPER.writeValueAsString(new ZeroBean("test"));

            assertThat(json).contains("\"value\":\"****\"");
        }
    }

    @Nested
    class MaskedSerializerDirectTests {

        @Test
        void masksThroughSerializer() throws JsonProcessingException {
            var mapper = JsonMapper.builder()
                    .addModule(new MaskingModule())
                    .build();
            record Simple(@MaskValue(MaskStrategy.PASSWORD) String secret) {
            }

            String json = mapper.writeValueAsString(new Simple("mypassword"));

            assertThat(json).contains("\"secret\":\"******\"");
        }
    }
}
